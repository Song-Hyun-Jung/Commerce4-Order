package com.digital4.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.digital4.context.InventoryOrderContext;
import com.digital4.context.PersonOrderContext;
import com.digital4.context.ProductOrderContext;
import com.digital4.schema.Address;
import com.digital4.schema.Cart;
import com.digital4.schema.Inventory;
import com.digital4.schema.Phone;
import com.digital4.schema.Prepurchase;
import com.digital4.schema.Product;
import com.digital4.sql.mapper.CartMapper;
import com.digital4.sql.mapper.PrepurchaseMapper;
import com.digital4.sql.vo.AddressVO;
import com.digital4.sql.vo.PartyPrepurchaseVO;
import com.digital4.sql.vo.PhoneVO;
import com.digital4.sql.vo.PrepurchaseVO;

@Component
public class PrepurchaseService {
	@Autowired
	PrepurchaseMapper prepurchaseMapper;
	@Autowired
	CartMapper cartMapper;
	@Resource
	CartService cartSvc;
	@Autowired
	PersonOrderContext personContext;
	@Autowired
	ProductOrderContext productContext;
	@Autowired
	InventoryOrderContext inventoryContext;	

	//personId에 해당하는 전체 가주문서 가져오기
	public List<Prepurchase> getAllPrepurchase(String personId, String token) throws Exception{
		try {
			List<PrepurchaseVO> ppVOList = prepurchaseMapper.getAllPrepurchase(Long.parseLong(personId));
			List<Prepurchase> allPrepurchase = new ArrayList<>();
			List<Cart> getCartList = new ArrayList<>();
			for(PrepurchaseVO ppVO : ppVOList) {
				Prepurchase pp = new Prepurchase();
				pp.setOrderId(ppVO.getOrderId());
				pp.setPersonId(ppVO.getPersonId());
				
				//HttpUrlConnection-바운디드 컨텍스트
				Address searchAddress = personContext.addressSearchById(ppVO.getAddressId(), token);
				pp.setAddress(searchAddress);
				
				//HttpUrlConnection-바운디드 컨텍스트
				Phone searchPhone= personContext.phoneSearchById(ppVO.getPhoneId(), token);
				pp.setPhone(searchPhone);
				
				for(long cartId : ppVO.getCartIdList()) {
					Cart cart = new Cart();
					cart = cartSvc.getCart(cartId);
					getCartList.add(cart);
				}
				pp.setCartList(getCartList);
				getCartList = new ArrayList<>();
				allPrepurchase.add(pp);
			}
			return allPrepurchase;
			
		}catch(Exception e) {
			throw e;
		}
	}
	
	
	//주문상세 오류 확인
	public boolean checkPrepurchase(Prepurchase prepurchase, String token) throws Exception{
		Map<Long, Long> checkInvenQuantity = new HashMap<Long, Long>(); //카트에 있는 같은 아이템들의 개수가 총 몇개인지 확인
		if(prepurchase.getCartList().size() == 0) {
			throw new Exception("상품을 넣지 않았습니다.");
		}
		for(Cart cart : prepurchase.getCartList()) {
			if(cartSvc.getCart(cart.getCartId()).getCartId() == 0 ) {
				throw new Exception("장바구니에 없습니다.");
			}
		
			//HttpUrlConnection-바운디드 컨텍스트
			Product product = productContext.productSearchById(cart.getProductId(), token);
			if(product.getProductName() == null){
				throw new Exception("존재하지 않는 상품입니다.");
			}
			if(cart.getProductQuantity() == 0) {
				throw new Exception("수량이 입력되지 않은 상태입니다.");
			}
				
			//주문상세에 있는 같은 아이템들의 개수가 총 몇개인지 확인
			//HttpUrlConnection-바운디드 컨텍스트
			Inventory inventory = inventoryContext.inventorySearchById(cart.getProductId(), token);
			if(cart.getProductQuantity() > inventory.getQuantity()) {
				throw new Exception("재고가 부족합니다.");
			}
			
			if(checkInvenQuantity.get(cart.getProductId()) == null) {
				checkInvenQuantity.put(cart.getProductId(), cart.getProductQuantity());
			}
			else {
				long curProductQuantity = checkInvenQuantity.get(cart.getProductId()); //현재 주문상세에 담긴 하나의 상품의 개수
				if((curProductQuantity + cart.getProductQuantity()) > inventory.getQuantity()) {
					throw new Exception("재고가 부족합니다.");
				}
				checkInvenQuantity.replace(cart.getProductId(), curProductQuantity + cart.getProductQuantity());
			}
		}
		if(prepurchase.getPhone().getPhoneNumber() == null) {
			throw new Exception("전화번호가 입력되지 않았습니다.");
		}
	
		if(prepurchase.getAddress().getAddressDetail() == null) {
			throw new Exception("주소가 입력되지 않았습니다.");
		}
		return true;
	}

	//가주문서 작성
	@Transactional
	public Prepurchase addPurchaseDetail(Prepurchase prepurchase, String personId, String token) throws Exception{
		prepurchase.setOrderId(System.currentTimeMillis());
		prepurchase.setPersonId(Long.parseLong(personId));
		
		try {	
			if(checkPrepurchase(prepurchase, token) == true) {	
				//주소
				//HttpUrlConnection-바운디드 컨텍스트
				Address searchAddress = personContext.addressSearchById(prepurchase.getAddress().getAddressId(), token);
				if(searchAddress.getAddressDetail() != null) {
					prepurchase.setAddress(searchAddress);
				}
				else { //기존에 저장되어있지 않은 주소의 경우
					
					AddressVO newAddrVO = new AddressVO(System.currentTimeMillis(), prepurchase.getAddress().getAddressDetail());
					//HttpUrlConnection-바운디드 컨텍스트
					Address newAddr = personContext.insertAddress(newAddrVO, token);
					prepurchase.setAddress(newAddr);
				}
							
				//전화번호
				//HttpUrlConnection-바운디드 컨텍스트
				Phone searchPhone = personContext.phoneSearchById(prepurchase.getPhone().getPhoneId(), token);
				if(searchPhone.getPhoneNumber() != null) {
					prepurchase.setPhone(searchPhone);
				}
				else { //기존에 저장되어있지 않은 전화번호의 경우
					PhoneVO newPhoneVO = new PhoneVO(System.currentTimeMillis(), prepurchase.getPhone().getPhoneNumber());
					//HttpUrlConnection-바운디드 컨텍스트
					Phone newPhone = personContext.insertPhone(newPhoneVO, token);
					prepurchase.setPhone(newPhone);
				}
				
				//장바구니의 수량과 입력한 수량값이 다를때 장바구니의 수량을 입력한 수량값으로 변경
				for(Cart c : prepurchase.getCartList()) { 
					Cart searchCart = cartSvc.getCart(c.getCartId());
					if(c.getProductQuantity() != searchCart.getProductQuantity()) {
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("newQuantity", c.getProductQuantity());	
						map.put("cartId", c.getCartId());

						int result = cartMapper.updateCartQuantity(map);
						if(result == 0) {
							throw new Exception("카트 수량 변경 오류");
						}
					}
				}
					
				PrepurchaseVO ppVO = new PrepurchaseVO();
				ppVO.setOrderId(prepurchase.getOrderId());
				ppVO.setPersonId(prepurchase.getPersonId());
				ppVO.setAddressId(prepurchase.getAddress().getAddressId());
				ppVO.setPhoneId(prepurchase.getPhone().getPhoneId());
				for(Cart c : prepurchase.getCartList()) {
					PartyPrepurchaseVO pppVO = new PartyPrepurchaseVO(ppVO.getOrderId(), c.getCartId());
					prepurchaseMapper.insertPartyPrepurchase(pppVO);
				}
				
				System.out.println(ppVO.getOrderId());
				prepurchaseMapper.addPurchaseDetail(ppVO);
				
				return getPrepurchase(""+ppVO.getOrderId(), token);
			}
			return null;
		}catch(Exception e) {
				throw e;
		}
	}

	//가주문서 하나 조회
	public Prepurchase getPrepurchase(String prepurchaseId, String token) throws Exception {
		
		try {
			PrepurchaseVO ppVO = prepurchaseMapper.getPrepurchase(Long.parseLong(prepurchaseId));
			Prepurchase pp = new Prepurchase();
			
			List<Cart> getCartList = new ArrayList<>();
			if(ppVO != null) {
				pp.setOrderId(ppVO.getOrderId());
				pp.setPersonId(ppVO.getPersonId());
				
				//HttpUrlConnection-바운디드 컨텍스트
				Address searchAddress = personContext.addressSearchById(ppVO.getAddressId(), token);
				
				pp.setAddress(searchAddress);
				
				//HttpUrlConnection-바운디드 컨텍스트
				Phone searchPhone = personContext.phoneSearchById(ppVO.getPhoneId(), token);
				pp.setPhone(searchPhone);
				
				for(long cartId : ppVO.getCartIdList()) {
					Cart cart = new Cart();
					cart = cartSvc.getCart(cartId);
					getCartList.add(cart);
				}
				pp.setCartList(getCartList);
			}
			return pp;
		}catch(Exception e) {
			throw e;
		}
	}
}
