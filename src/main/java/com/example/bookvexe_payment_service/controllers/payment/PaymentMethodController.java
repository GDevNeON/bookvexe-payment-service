package com.example.bookvexe_payment_service.controllers.payment;

import com.example.bookvexe_payment_service.models.dto.payment.*;
import com.example.bookvexe_payment_service.services.payment.PaymentMethodService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/payment-methods")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    public PaymentMethodController(PaymentMethodService paymentMethodService) {
        this.paymentMethodService = paymentMethodService;
    }

    @GetMapping
    public ResponseEntity<List<PaymentMethodResponse>> findAll() {
        return ResponseEntity.ok(paymentMethodService.findAll());
    }

    @GetMapping("/pagination")
    public ResponseEntity<Page<PaymentMethodResponse>> findAll(PaymentMethodQuery query) {
        return ResponseEntity.ok(paymentMethodService.findAll(query));
    }

    @PostMapping("/pagination")
    public ResponseEntity<Page<PaymentMethodResponse>> findAll2(@RequestBody PaymentMethodQuery query) {
        return ResponseEntity.ok(paymentMethodService.findAll(query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethodResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentMethodService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PaymentMethodResponse> create(@RequestBody PaymentMethodCreate createDto) {
        return ResponseEntity.ok(paymentMethodService.create(createDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentMethodResponse> update(@PathVariable UUID id, @RequestBody PaymentMethodUpdate updateDto) {
        return ResponseEntity.ok(paymentMethodService.update(id, updateDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        paymentMethodService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/activate/{id}")
    public ResponseEntity<Void> activate(@PathVariable UUID id) {
        paymentMethodService.activate(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        paymentMethodService.deactivate(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/select")
    public ResponseEntity<List<PaymentMethodSelectResponse>> findAllForSelect() {
        return ResponseEntity.ok(paymentMethodService.findAllForSelect());
    }

    @GetMapping("/select/pagination")
    public ResponseEntity<Page<PaymentMethodSelectResponse>> findAllForSelect(PaymentMethodQuery query) {
        return ResponseEntity.ok(paymentMethodService.findAllForSelect(query));
    }

    @PostMapping("/select/pagination")
    public ResponseEntity<Page<PaymentMethodSelectResponse>> findAllForSelect2(@RequestBody PaymentMethodQuery query) {
        return ResponseEntity.ok(paymentMethodService.findAllForSelect(query));
    }
}
