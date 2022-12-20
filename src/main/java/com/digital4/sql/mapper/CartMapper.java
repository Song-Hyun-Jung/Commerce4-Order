package com.digital4.sql.mapper;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.digital4.sql.vo.CartVO;

@Mapper
public interface CartMapper {
	public List<CartVO> getAllCart(long personId);
	public int insertCart(CartVO cart);
	public CartVO getCart(long cartId);
	public int deleteCart(long cartId);
	public int updateCartQuantity(HashMap<String, Object> hashmap);
}
