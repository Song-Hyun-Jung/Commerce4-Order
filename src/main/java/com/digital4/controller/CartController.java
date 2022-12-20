package com.digital4.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.digital4.schema.Cart;
import com.digital4.schema.ErrorMsg;
import com.digital4.schema.SuccessMsg;
import com.digital4.service.AuthInterceptService;
import com.digital4.service.CartService;
import com.digital4.utils.ExceptionUtils;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "장바구니", description = "Cart Related API")
@RequestMapping(value = "/rest/cart")
public class CartController {
	
	@Resource
	CartService cartSvc;
	@Resource
	AuthInterceptService authSvc;
	
	
	@RequestMapping(value = "/insertCart", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "장바구니 추가", notes = "장바구니에 추가를 위한 API.")
	@ApiResponses({ @ApiResponse(code = 200, message = "성공", response = Cart.class),
			@ApiResponse(code = 500, message = "실패", response = ErrorMsg.class) })
	public ResponseEntity<?> insertCart(
			@Parameter(name = "장바구니에 상품을 추가", description = "", required = true) @RequestBody Cart cart, HttpServletRequest req) {
		MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
		ErrorMsg errors = new ErrorMsg();
		Cart cart_res = new Cart();
		try {
			String token = req.getHeader("Authorization");
			String personId = ""+authSvc.getAuthPersonId(Long.parseLong(token));
			System.out.println(personId);
		
			cartSvc.addCart(cart, personId, token);
			
			cart_res = cartSvc.getCart(cart.getCartId());
		} catch (Exception e) {
			return ExceptionUtils.setException(errors, 500, e.getMessage(), header);
		}
		
		return new ResponseEntity<Cart>(cart_res, header, HttpStatus.valueOf(200)); // ResponseEntity를 활용한 응답 생성
	}
	
	@RequestMapping(value = "/getCart", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "장바구니 조회", notes = "장바구니에 담긴 전체 상품을 조회하는 API.")
	@ApiResponses({ @ApiResponse(code = 200, message = "성공", response = Cart.class),
			@ApiResponse(code = 500, message = "실패", response = ErrorMsg.class) })
	public ResponseEntity<?> getCart(HttpServletRequest req) {
		MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();

		ErrorMsg errors = new ErrorMsg();
		
		try {
			String token = req.getHeader("Authorization");
			List<Cart> cartList = cartSvc.getAllCart(""+authSvc.getAuthPersonId(Long.parseLong(token))); //파라미터 따로 받지 않고 request를 보냄
			return new ResponseEntity<List<Cart>>(cartList, header, HttpStatus.valueOf(200));  // ResponseEntity를 활용한 응답 생성
		} catch (Exception e) {
			return ExceptionUtils.setException(errors, 500, e.getMessage(), header);
		}

	}
	
	
}

