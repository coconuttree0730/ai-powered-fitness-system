package com.fitness.modules.chat.tools;

import com.fitness.modules.product.model.vo.ProductVO;
import com.fitness.modules.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductQueryTools {

    private final ProductService productService;

    @Tool(description = """
            查询健身场馆的商品列表，包括运动器材、健身装备、营养补剂等。
            当用户询问"有哪些商品"、"有什么产品"、"卖什么东西"、"商品列表"、"查看商品"等问题时，必须调用此工具。
            支持按分类过滤，如"器材"、"装备"、"补剂"等。
            返回商品名称、价格、库存状态等信息。
            """)
    public List<ProductVO> listProducts(String category) {
        return productService.getProductList(category);
    }

    @Tool(description = """
            查询指定商品的详细信息。
            当用户询问某个具体商品的详情、价格、规格、库存、功能特点等问题时，必须调用此工具。
            例如"这款蛋白粉多少钱"、"瑜伽垫有什么规格"、"哑铃的详细信息"等。
            """)
    public ProductVO getProductDetail(Long productId) {
        return productService.getProductDetail(productId);
    }
}
