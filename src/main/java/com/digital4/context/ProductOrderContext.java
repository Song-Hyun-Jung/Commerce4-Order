package com.digital4.context;

import org.springframework.stereotype.Component;

import com.digital4.schema.Product;
import com.digital4.utils.HttpConnectionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ProductOrderContext {
	public Product productSearchById(long productId, String token) throws Exception {
		String url = "8080/rest/product/searchById/product";
		url += "/" + productId;
		
		String response = HttpConnectionUtils.getRequest(url, token);
		System.out.println("getRequest:" + response);
		ObjectMapper objectMapper = new ObjectMapper();
		Product searchProduct = objectMapper.readValue(response, Product.class);
		return searchProduct;
	}
	
	
}
