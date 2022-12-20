package com.digital4.sql.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.digital4.sql.vo.PurchaseVO;

@Mapper
public interface PurchaseMapper {
	public int insertPurchase(PurchaseVO purchaseVO);
	public List<PurchaseVO> getPurchaseHistory(long personId);
}
