package com.digital4.context;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.digital4.schema.Address;
import com.digital4.schema.Phone;
import com.digital4.sql.vo.AddressVO;
import com.digital4.sql.vo.PhoneVO;
import com.digital4.utils.HttpConnectionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class PersonOrderContext {
	public Address addressSearchById(long addressId, String token) throws Exception{
		String targetUrl = "APIG";
		String requestUrl = "/rest/address/searchById";
		requestUrl += "/" + addressId;
		
		String response = HttpConnectionUtils.getRequest(targetUrl, requestUrl, token);
		System.out.println("getRequest:" + response);
		ObjectMapper objectMapper = new ObjectMapper();
		Address searchAddress = objectMapper.readValue(response, Address.class);
		return searchAddress;
	}
	
	public Phone phoneSearchById(long phoneId, String token) throws Exception{
		String targetUrl = "APIG";
		String requestUrl = "/rest/phone/searchById";
		requestUrl += "/" + phoneId;
		
		String response = HttpConnectionUtils.getRequest(targetUrl, requestUrl, token);
		System.out.println("getRequest:" + response);
		ObjectMapper objectMapper = new ObjectMapper();
		Phone searchPhone = objectMapper.readValue(response, Phone.class);
		return searchPhone;
	}
	
	public Address insertAddress(AddressVO newAddrVO, String token) throws Exception{
		String targetUrl = "APIG";
		String requestUrl = "/rest/address/insertAddress";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("addressId", newAddrVO.getAddressId());
		map.put("addressDetail", newAddrVO.getAddressDetail());
		String response = HttpConnectionUtils.postRequest(targetUrl, requestUrl, map, token);
		System.out.println("postRequest:" + response);
		ObjectMapper objectMapper = new ObjectMapper();
		Address newAddr = objectMapper.readValue(response, Address.class);
		return newAddr;
	}
	
	public Phone insertPhone(PhoneVO newPhoneVO , String token) throws Exception{
		String targetUrl = "APIG";
		String requestUrl = "/rest/phone/insertPhone";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("phoneId", newPhoneVO.getPhoneId());
		map.put("phoneNumber", newPhoneVO.getPhoneNumber());
		String response = HttpConnectionUtils.postRequest(targetUrl, requestUrl, map, token);
		System.out.println("postRequest:" + response);
		ObjectMapper objectMapper = new ObjectMapper();
		Phone newPhone = objectMapper.readValue(response, Phone.class);
		return newPhone;
	}
}	
