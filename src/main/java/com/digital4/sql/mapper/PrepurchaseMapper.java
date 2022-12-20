package com.digital4.sql.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.digital4.sql.vo.PartyPrepurchaseVO;
import com.digital4.sql.vo.PrepurchaseVO;

@Mapper
public interface PrepurchaseMapper {
	public PrepurchaseVO getPrepurchase(long orderId);
	public List<PrepurchaseVO> getAllPrepurchase(long personId);
	public int addPurchaseDetail(PrepurchaseVO prepurchaseVO);
	public int insertPartyPrepurchase(PartyPrepurchaseVO pppVO);
}
