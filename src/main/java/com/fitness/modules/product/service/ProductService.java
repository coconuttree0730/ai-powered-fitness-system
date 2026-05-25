package com.fitness.modules.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.modules.product.model.dto.ProductDTO;
import com.fitness.modules.product.model.dto.StockUpdateDTO;
import com.fitness.modules.product.model.entity.Product;
import com.fitness.modules.product.model.vo.ProductVO;

import java.util.List;

public interface ProductService extends IService<Product> {

    List<ProductVO> getProductList(String category);

    List<ProductVO> getAllProducts(String category, String status, String keyword);

    ProductVO getProductDetail(Long id);

    ProductVO createProduct(ProductDTO dto);

    ProductVO updateProduct(ProductDTO dto);

    void deleteProduct(Long id);

    void updateProductStatus(Long id, String status);

    ProductVO updateStock(Long id, StockUpdateDTO dto);

    List<ProductVO> getProductsByCoachId(Long coachId);

    void deleteCoachProduct(Long productId, Long coachId);

    void updateCoachProductStatus(Long productId, Long coachId, String status);
}
