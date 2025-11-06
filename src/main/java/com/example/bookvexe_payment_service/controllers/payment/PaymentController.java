package com.example.bookvexe_payment_service.controllers.payment;

import com.example.bookvexe_payment_service.models.dto.payment.*;
import com.example.bookvexe_payment_service.services.payment.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> findAll() {
        return ResponseEntity.ok(paymentService.findAll());
    }

    @GetMapping("/pagination")
    public ResponseEntity<Page<PaymentResponse>> findAll(PaymentQuery query) {
        return ResponseEntity.ok(paymentService.findAll(query));
    }

    @PostMapping("/pagination")
    public ResponseEntity<Page<PaymentResponse>> findAll2(@RequestBody PaymentQuery query) {
        return ResponseEntity.ok(paymentService.findAll(query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@RequestBody PaymentCreate createDto) {
        return ResponseEntity.ok(paymentService.create(createDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentResponse> update(@PathVariable UUID id, @RequestBody PaymentUpdate updateDto) {
        return ResponseEntity.ok(paymentService.update(id, updateDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        paymentService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/activate/{id}")
    public ResponseEntity<Void> activate(@PathVariable UUID id) {
        paymentService.activate(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        paymentService.deactivate(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/select")
    public ResponseEntity<List<PaymentSelectResponse>> findAllForSelect() {
        return ResponseEntity.ok(paymentService.findAllForSelect());
    }

    @GetMapping("/select/pagination")
    public ResponseEntity<Page<PaymentSelectResponse>> findAllForSelect(PaymentQuery query) {
        return ResponseEntity.ok(paymentService.findAllForSelect(query));
    }

    @PostMapping("/select/pagination")
    public ResponseEntity<Page<PaymentSelectResponse>> findAllForSelect2(@RequestBody PaymentQuery query) {
        return ResponseEntity.ok(paymentService.findAllForSelect(query));
    }
}
