package com.example.bookvexe_payment_service.controllers.payment;

import com.example.bookvexe_payment_service.services.payment.PaymentService;
import com.example.bookvexe_payment_service.utils.VNPayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping({"/api", ""})
public class VNPayController {

    private final PaymentService paymentService;

    public VNPayController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    private String secretKey;

    @Value("${vnpay.payUrl}")
    private String vnp_PayUrl;

    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;

    @Value("${vnpay.ipnUrl}")
    private String vnp_IpnUrl;

    @Value("${vnpay.frontendReturnUrl}")
    private String frontendReturnUrl;

    @GetMapping("/create-payment")
    public Map<String, Object> createPayment(HttpServletRequest req,
                                             @RequestParam("amount") long amount,
                                             @RequestParam("orderId") String orderId) throws Exception {

        String ipAddress = req.getRemoteAddr();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", orderId);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang #" + orderId);
        vnp_Params.put("vnp_OrderType", "billpayment");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", ipAddress);

        String vnp_CreateDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        String paymentUrl = VNPayUtil.getPaymentUrl(vnp_Params, secretKey, vnp_PayUrl);

        Map<String, Object> response = new HashMap<>();
        response.put("paymentUrl", paymentUrl);
        return response;
    }

    @GetMapping({"/vnpay-return", "/vnpay-return/"})
    public ResponseEntity<Void> handleReturn(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        for (Enumeration<String> paramNames = request.getParameterNames(); paramNames.hasMoreElements();) {
            String paramName = paramNames.nextElement();
            params.put(paramName, request.getParameter(paramName));
        }

        String vnp_SecureHash = params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        for (Iterator<String> it = fieldNames.iterator(); it.hasNext();) {
            String fieldName = it.next();
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII);
                hashData.append(fieldName).append('=')
                        .append(encodedValue);
                if (it.hasNext()) hashData.append('&');
            }
        }

        String signValue = VNPayUtil.hmacSHA512(secretKey, hashData.toString());

        String orderId = params.getOrDefault("vnp_TxnRef", "");
        String responseCode = params.getOrDefault("vnp_ResponseCode", "");
        String status;

        boolean validSignature = signValue.equals(vnp_SecureHash);

        if (validSignature && !orderId.isEmpty()) {
            try {
                if ("00".equals(responseCode)) {
                    status = "success";
                    paymentService.updateStatusByTransactionCode(orderId, "SUCCESS", LocalDateTime.now());
                } else {
                    status = "failed";
                    if ("24".equals(responseCode)) {
                        // Customer cancelled the transaction
                        paymentService.updateStatusByTransactionCode(orderId, "CANCELLED", null);
                    } else if ("11".equals(responseCode)) {
                        // Payment timeout - remove the pending payment record
                        paymentService.deleteByTransactionCode(orderId);
                    } else {
                        // Other error codes
                        paymentService.updateStatusByTransactionCode(orderId, "FAILED", null);
                    }
                }
            } catch (Exception ex) {
                // In case of any problem updating payment, still continue redirecting user
                status = ("00".equals(responseCode) && validSignature) ? "success" : "failed";
            }
        } else {
            status = "failed";
        }

        String redirectUrl = String.format("%s?orderId=%s&status=%s&code=%s",
                frontendReturnUrl,
                orderId,
                status,
                responseCode);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", redirectUrl);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @RequestMapping(value = {"/vnpay-ipn", "/vnpay-ipn/"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String handleIpn(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        for (Enumeration<String> paramNames = request.getParameterNames(); paramNames.hasMoreElements();) {
            String paramName = paramNames.nextElement();
            params.put(paramName, request.getParameter(paramName));
        }

        String vnp_SecureHash = params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        for (Iterator<String> it = fieldNames.iterator(); it.hasNext();) {
            String fieldName = it.next();
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII);
                hashData.append(fieldName).append('=')
                        .append(encodedValue);
                if (it.hasNext()) hashData.append('&');
            }
        }

        String signValue = VNPayUtil.hmacSHA512(secretKey, hashData.toString());

        String orderId = params.getOrDefault("vnp_TxnRef", "");
        String responseCode = params.getOrDefault("vnp_ResponseCode", "");

        boolean validSignature = signValue.equals(vnp_SecureHash);

        if (validSignature && !orderId.isEmpty()) {
            try {
                if ("00".equals(responseCode)) {
                    paymentService.updateStatusByTransactionCode(orderId, "SUCCESS", LocalDateTime.now());
                } else {
                    if ("24".equals(responseCode)) {
                        paymentService.updateStatusByTransactionCode(orderId, "CANCELLED", null);
                    } else if ("11".equals(responseCode)) {
                        paymentService.deleteByTransactionCode(orderId);
                    } else {
                        paymentService.updateStatusByTransactionCode(orderId, "FAILED", null);
                    }
                }
            } catch (Exception ex) {
                // ignore errors to avoid breaking VNPay callback contract
            }
        }

        // Always respond with success code so VNPay stops retrying
        return "responseCode=00";
    }
}
