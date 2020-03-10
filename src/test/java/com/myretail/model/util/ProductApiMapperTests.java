package com.myretail.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.myretail.model.Product;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

public class ProductApiMapperTests {

  private static final String JSON_DATA = "{\"product\":{\"available_to_promise_network\":{\"product_id\":\"13860428\",\"id_type\":\"TCIN\",\"available_to_promise_quantity\":4.0,\"street_date\":\"2011-11-15T06:00:00.000Z\",\"availability\":\"AVAILABLE\",\"online_available_to_promise_quantity\":4.0,\"stores_available_to_promise_quantity\":0.0,\"availability_status\":\"LIMITED_STOCK\",\"multichannel_options\":[],\"is_infinite_inventory\":false,\"loyalty_availability_status\":\"LIMITED_STOCK\",\"loyalty_purchase_start_date_time\":\"1970-01-01T00:00:00.000Z\",\"is_loyalty_purchase_enabled\":false,\"is_out_of_stock_in_all_store_locations\":false,\"is_out_of_stock_in_all_online_locations\":false},\"price\":{\"partNumber\":\"13860428\",\"listPrice\":{\"null\":false,\"price\":12.49,\"formattedPrice\":\"$12.49\",\"priceType\":\"Reg\",\"maxPrice\":0,\"minPrice\":0},\"offerPrice\":{\"null\":false,\"startDate\":1576742400000,\"price\":12.49,\"eyebrow\":\"\",\"formattedPrice\":\"$12.49\",\"saveDollar\":0.0,\"priceType\":\"Reg\",\"endDate\":253402214400000,\"maxPrice\":0,\"savePercent\":0,\"minPrice\":0},\"ppu\":\"\",\"mapPriceFlag\":\"N\",\"channelAvailability\":\"0\"},\"item\":{\"tcin\":\"13860428\",\"bundle_components\":{},\"dpci\":\"058-34-0436\",\"upc\":\"025192110306\",\"product_description\":{\"title\":\"The Big Lebowski (Blu-ray)\",\"downstream_description\":\"Jeff \\\"The Dude\\\" Lebowski (Bridges) is the victim of mistaken identity. Thugs break into his apartment in the errant belief that they are accosting Jeff Lebowski, the eccentric millionaire philanthropist, not the laid-back, unemployed Jeff Lebowski. In the aftermath, \\\"The Dude\\\" seeks restitution from his wealthy namesake. He and his buddies (Goodman and Buscemi) are swept up in a kidnapping plot that quickly spins out of control.\",\"bullet_description\":[\"<B>Movie Studio:</B> Universal Studios\",\"<B>Movie Genre:</B> Comedy\",\"<B>Run Time (minutes):</B> 119\",\"<B>Software Format:</B> Blu-ray\"]},\"buy_url\":\"https://www.target.com/p/the-big-lebowski-blu-ray/-/A-13860428\",\"enrichment\":{\"images\":[{\"base_url\":\"https://target.scene7.com/is/image/Target/\",\"primary\":\"GUEST_44aeda52-8c28-4090-85f1-aef7307ee20e\",\"content_labels\":[{\"image_url\":\"GUEST_44aeda52-8c28-4090-85f1-aef7307ee20e\"}]}],\"sales_classification_nodes\":[{\"node_id\":\"hp0vg\"},{\"node_id\":\"5xswx\"}]},\"return_method\":\"This item can be returned to any Target store or Target.com.\",\"handling\":{},\"recall_compliance\":{\"is_product_recalled\":false},\"tax_category\":{\"tax_class\":\"G\",\"tax_code_id\":99999,\"tax_code\":\"99999\"},\"display_option\":{\"is_size_chart\":false},\"fulfillment\":{\"is_po_box_prohibited\":true,\"po_box_prohibited_message\":\"We regret that this item cannot be shipped to PO Boxes.\",\"box_percent_filled_by_volume\":0.27,\"box_percent_filled_by_weight\":0.43,\"box_percent_filled_display\":0.43},\"package_dimensions\":{\"weight\":\"0.18\",\"weight_unit_of_measure\":\"POUND\",\"width\":\"5.33\",\"depth\":\"6.65\",\"height\":\"0.46\",\"dimension_unit_of_measure\":\"INCH\"},\"environmental_segmentation\":{\"is_hazardous_material\":false,\"has_lead_disclosure\":false},\"manufacturer\":{},\"product_vendors\":[{\"id\":\"1984811\",\"manufacturer_style\":\"025192110306\",\"vendor_name\":\"Ingram Entertainment\"},{\"id\":\"4667999\",\"manufacturer_style\":\"61119422\",\"vendor_name\":\"UNIVERSAL HOME VIDEO\"},{\"id\":\"1979650\",\"manufacturer_style\":\"61119422\",\"vendor_name\":\"Universal Home Ent PFS\"}],\"product_classification\":{\"product_type\":\"542\",\"product_type_name\":\"ELECTRONICS\",\"item_type_name\":\"Movies\",\"item_type\":{\"category_type\":\"Item Type: MMBV\",\"type\":300752,\"name\":\"movies\"}},\"product_brand\":{\"brand\":\"Universal Home Video\",\"manufacturer_brand\":\"Universal Home Video\",\"facet_id\":\"55zki\"},\"item_state\":\"READY_FOR_LAUNCH\",\"specifications\":[],\"attributes\":{\"gift_wrapable\":\"Y\",\"has_prop65\":\"N\",\"is_hazmat\":\"N\",\"manufacturing_brand\":\"Universal Home Video\",\"max_order_qty\":10,\"street_date\":\"2011-11-15\",\"media_format\":\"Blu-ray\",\"merch_class\":\"MOVIES\",\"merch_classid\":58,\"merch_subclass\":34,\"return_method\":\"This item can be returned to any Target store or Target.com.\",\"ship_to_restriction\":\"United States Minor Outlying Islands,American Samoa (see also separate entry under AS),Puerto Rico (see also separate entry under PR),Northern Mariana Islands,Virgin Islands, U.S.,APO/FPO,Guam (see also separate entry under GU)\"},\"country_of_origin\":\"US\",\"relationship_type_code\":\"Stand Alone\",\"subscription_eligible\":false,\"ribbons\":[],\"tags\":[],\"ship_to_restriction\":\"This item cannot be shipped to the following locations: United States Minor Outlying Islands, American Samoa, Puerto Rico, Northern Mariana Islands, Virgin Islands, U.S., APO/FPO, Guam\",\"estore_item_status_code\":\"A\",\"is_proposition_65\":false,\"return_policies\":{\"user\":\"Regular Guest\",\"policyDays\":\"30\",\"guestMessage\":\"This item must be returned within 30 days of the in-store purchase, ship date, or online order pickup. See return policy for details.\"},\"gifting_enabled\":false,\"packaging\":{\"is_retail_ticketed\":false}},\"circle_offers\":{\"universal_offer_exists\":false,\"non_universal_offer_exists\":true}}}";

  @Test
  void empty_json() {
    JsonObject object = new JsonObject();
    assertThrows(InvalidJsonData.class, () -> ProductApiMapper.parseApiJson(object));
  }

  @Test
  void valid_json() throws Exception {
    JsonObject object = new JsonObject(JSON_DATA);
    Product product = ProductApiMapper.parseApiJson(object);
    assertEquals(13860428, product.getId());
    assertEquals("The Big Lebowski (Blu-ray)", product.getTitle());
  }

  @Test
  void invalid_json() {
    JsonObject object = new JsonObject("{}");
    assertThrows(InvalidJsonData.class, () -> ProductApiMapper.parseApiJson(object));
  }
}
