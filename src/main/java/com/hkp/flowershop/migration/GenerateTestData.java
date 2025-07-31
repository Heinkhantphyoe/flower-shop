package com.hkp.flowershop.migration;


import com.hkp.flowershop.model.Category;
import com.hkp.flowershop.model.Product;
import com.hkp.flowershop.repository.CategoryRepo;
import com.hkp.flowershop.repository.ProductRepo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateTestData {


    private final CategoryRepo categoryRepo;
    private final ProductRepo productRepo;
    private final Random random = new Random();

    @PostConstruct
    public void seed() {
        String[] categoryNames = {"Flower", "Gift", "Cake"};
        for (String name : categoryNames) {
            categoryRepo.findByName(name).orElseGet(() -> {
                Category c = new Category();
                c.setName(name);
                return categoryRepo.save(c);
            });
        }

        List<Category> categories = categoryRepo.findAll();

        if (productRepo.count() > 0) {
            log.info("✔️ Products already seeded.");
            return;
        }

        //add 200 product to test
        for (int i = 1; i <= 200; i++) {
            Product p = new Product();
            p.setName("Test Product " + i);
            p.setDescription("This is test product number " + i);
            p.setPrice(10 + random.nextDouble() * 90); // price 10–100
            p.setStock(1 + random.nextInt(50)); // stock 1–50

            Category category = categories.get(i % categories.size());
            p.setCategory(category);

            String categoryName = category.getName().toLowerCase();
            String imageUrl = switch (categoryName) {
                case "flower" ->
                        "flower.avif";
                case "gift" ->
                        "gift.avif";
                case "cake" ->
                        "cake.avif";
                default -> "not-found.avif";
            };

            p.setImageUrl(imageUrl);

            productRepo.save(p);
        }

        log.info("Seeded categories and 200 test products.");
    }
}
