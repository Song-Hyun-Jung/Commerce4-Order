package com.digital4.context;

import org.springframework.stereotype.Component;

import com.digital4.schema.Product;
import com.digital4.utils.HttpConnectionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ProductOrderContext {
	public Product productSearchById(long productId, String token) throws Exception {
		String targetUrl = "APIG";
		String requestUrl = "/rest/product/searchById/product";
		requestUrl += "/" + productId;
		
		String response = HttpConnectionUtils.getRequest(targetUrl, requestUrl, token);
		System.out.println("getRequest:" + response);
		ObjectMapper objectMapper = new ObjectMapper();
		Product searchProduct = objectMapper.readValue(response, Product.class);
		return searchProduct;
	}
	
	
}
