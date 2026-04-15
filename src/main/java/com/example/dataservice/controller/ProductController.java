package com.example.dataservice.controller;

import com.example.dataservice.domain.Product;
import com.example.dataservice.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;
    private final EntityManager entityManager;

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ProductResponse> update(@PathVariable Long id,
                                                   @RequestBody ProductRequest body) {
        return productRepository.findById(id)
                .map(product -> {
                    if (body.name()     != null) product.setName(body.name());
                    if (body.category() != null) product.setCategory(body.category());
                    if (body.price()    != null) product.setPrice(body.price());
                    if (body.stock()    != null) product.setStock(body.stock());
                    if (body.active()   != null) product.setActive(body.active());
                    return ResponseEntity.ok(toResponse(productRepository.save(product)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/audit/revisions")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Number>> getRevisions(@PathVariable Long id) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        List<Number> revisions = reader.getRevisions(Product.class, id);
        return ResponseEntity.ok(revisions);
    }

    @GetMapping("/{id}/audit/{revision}")
    @Transactional(readOnly = true)
    public ResponseEntity<ProductResponse> getAtRevision(@PathVariable Long id,
                                                          @PathVariable Integer revision) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        Product product = reader.find(Product.class, id, revision);
        if (product == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toResponse(product));
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getCategory(),
                p.getPrice(), p.getStock(), p.getActive());
    }

    record ProductRequest(String name, String category, BigDecimal price, Integer stock, Boolean active) {}
    record ProductResponse(Long id, String name, String category, BigDecimal price, Integer stock, Boolean active) {}
}
