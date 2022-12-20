package com.digital4.context;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.digital4.schema.Inventory;
import com.digital4.utils.HttpConnectionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class InventoryOrderContext {
	//public boolean subtractQuantity//여기서 httpUrlConnection 호출
	public Inventory inventorySearchById(long inventoryId, String token) throws Exception{
		String url = "8080/rest/inventory/searchById";
		url += "/" + inventoryId;
		
		String response = HttpConnectionUtils.getRequest(url, token);
		System.out.println("getRequest:" + response);
		ObjectMapper objectMapper = new ObjectMapper();
		Inventory inventory = objectMapper.readValue(response, Inventory.class);
		return inventory;
	}
	
	public Inventory subtractQuantity(long inventoryId, long subtractQuantity, String token) throws Exception{
		String url = "8080/rest/inventory/purchaseInven";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("productId", inventoryId);
		map.put("quantity", subtractQuantity);
		String response = HttpConnectionUtils.postRequest(url, map, token);
		System.out.println("postRequest:" + response);
		ObjectMapper objectMapper = new ObjectMapper();
		Inventory subtractResult = objectMapper.readValue(response, Inventory.class);
		return subtractResult;
	}
}
