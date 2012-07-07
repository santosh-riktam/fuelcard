package com.fuelcard;


public class DBContract {

	public interface Tables {
		String TBL_CARDS = "Cards";
		String TBL_SITE_CARD = "SiteCard";
		String TBL_SITES = "Sites";

	}

	public interface CardsColumns {
		String cId = "c_id";
		String cardId = "CardID";
		String cardName = "CardName";
		String imgLarge = "ImgLarge";
		String imgMedium = "ImgMedium";
		String imgSmall = "ImgSmall";
	}
	
	
}
