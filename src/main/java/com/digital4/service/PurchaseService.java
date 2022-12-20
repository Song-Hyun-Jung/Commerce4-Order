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
import com.digital4.context.ProductOrderContext;
import com.digital4.schema.Cart;
import com.digital4.schema.Inventory;
import com.digital4.schema.Prepurchase;
import com.digital4.schema.Product;
import com.digital4.schema.Purchase;
import com.digital4.schema.PurchaseResult;
import com.digital4.sql.mapper.PurchaseMapper;
import com.digital4.sql.vo.PurchaseVO;
import com.digital4.utils.HttpConnectionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class PurchaseService {
	
	@Autowired
	PurchaseMapper purchaseMapper;
	@Resource
	CartService cartSvc;
	@Resource
	PrepurchaseService prepurchaseSvc;
	@Autowired
	ProductOrderContext productContext;
	@Autowired
	InventoryOrderContext inventoryContext;


	//구매->인벤토리 수량 차감, 데이터베이스 저장
	@Transactional
	public PurchaseResult purchase(Purchase purchase, String personId, String token) throws Exception {
	
		long totalPrice = 0;
		
		long purchaseId = System.currentTimeMillis(); // 구매 id
		long orderId = purchase.getOrderId(); //주문 상세 id
		
		if(orderId == 0) {
			throw new Exception("주문 상세가 입력되지 않았습니다.");
		}
		
		Prepurchase prepurchase = prepurchaseSvc.getPrepurchase(""+orderId, token); //주문 상세 객체 가져오기
		//System.out.println(prepurchase.getOrderId());
		if(prepurchase.getOrderId() == 0) {
			throw new Exception("주문 상세가 잘못되었습니다.");
		}
		
		PurchaseResult purchaseResult = new PurchaseResult();
		purchaseResult.setPurchaseId(purchaseId);
		purchaseResult.setOrderId(orderId);

		List<Cart> purchaseProductList = prepurchase.getCartList();
		
		//구매수량 인벤토리에서 감소, 총 가격 계산
		for(Cart c : purchaseProductList) {
			//상품 정보 가져오기
			//HttpUrlConnection-바운디드 컨텍스트
			Product product = productContext.productSearchById(c.getProductId(), token);
			
			//인벤토리 정보 가져오기
			//HttpUrlConnection-바운디드 컨텍스트
			Inventory inventory = inventoryContext.inventorySearchById(c.getProductId(), token);
			if(inventory.getQuantity() < c.getProductQuantity() || inventory.getQuantity() <= 0) { //주문 상세 이후에 다른사람의 구매로 재고가 부족할수 있음
				throw new Exception("수량 부족. 구매 실패");
			}
			
			//구매-수량감소
			//HttpUrlConnection-바운디드 컨텍스트
			Inventory subtractResult = inventoryContext.subtractQuantity(inventory.getProductId(), c.getProductQuantity(), token);
			if(subtractResult.getProductId() != 0)
				totalPrice += product.getPrice() * c.getProductQuantity();	
		}
		
		purchaseResult.setPersonId(prepurchase.getPersonId());
		purchaseResult.setPurchaseProductList(purchaseProductList); //카트
		purchaseResult.setTotalPrice(totalPrice);
	
		PurchaseVO pv = new PurchaseVO(purchaseResult.getPurchaseId(), purchaseResult.getOrderId(), purchaseResult.getPersonId());
		purchaseMapper.insertPurchase(pv);
		cartSvc.deleteCart(purchaseProductList); //구매한 상품 장바구니에서 삭제
		
		return purchaseResult;
	}

	
	//주문내역 확인
	public List<Purchase> getPurchaseHistory(String personId) throws Exception{
		try {
			List<PurchaseVO> pVOList = purchaseMapper.getPurchaseHistory(Long.parseLong(personId));
			List<Purchase> histList = new ArrayList<>();
			
			for(PurchaseVO pVO : pVOList) {
				Purchase purchase = new Purchase();
				purchase.setPurchaseId(pVO.getPurchaseId());
				purchase.setPersonId(pVO.getPersonId());
				purchase.setOrderId(pVO.getOrderId());
				histList.add(purchase);
			}

			return histList;
		}catch(Exception e) {
			throw e;
		}
	}
	
}
