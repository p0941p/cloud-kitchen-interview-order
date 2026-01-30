package com.css.challenge.client;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Order is a json-friendly representation of an order. */
public class Order {
  private final String id; // order id
  private final String name; // food name
  private final String temp; // ideal temperature
  private final int price; // price in dollars
  private int freshness; // freshness in seconds
  private String storage;
  private Instant timestamp;
  
  public Order(
      @JsonProperty("id") String id,
      @JsonProperty("name") String name,
      @JsonProperty("temp") String temp,
      @JsonProperty("price") int price,
      @JsonProperty("freshness") int freshness) {
    this.id = id;
    this.name = name;
    this.temp = temp;
    this.price = price;
    this.freshness = freshness;
  }

  static List<Order> parse(String json) throws JsonProcessingException {
    return new ObjectMapper().readValue(json, new TypeReference<List<Order>>() {});
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getTemp() {
    return temp;
  }

  public int getPrice() {
    return price;
  }

  public int getFreshness() {
    return freshness;
  }
  
  public String getStorage() {
	return storage;
  }


  public void setFreshness(int freshness) {
	this.freshness = freshness;
  }

  public void setStorage(String storage) {
	this.storage = storage;
  }

  public Instant getTimestamp() {
	return timestamp;
  }

  public void setTimestamp(Instant timestamp) {
	this.timestamp = timestamp;
  }

  @Override
  public String toString() {
    return "{id: "
        + id
        + ", name: "
        + name
        + ", temp: "
        + temp
        + ", price: $"
        + price
        + ", freshness:"
        + freshness
        + "}";
  }

  @Override
  public int hashCode() {
	return Objects.hash(freshness, id, name, price, storage, temp, timestamp);
  }

  @Override
  public boolean equals(Object obj) {
	if (this == obj)
		return true;
	if (obj == null)
		return false;
	if (getClass() != obj.getClass())
		return false;
	Order other = (Order) obj;
	return freshness == other.freshness && Objects.equals(id, other.id) && Objects.equals(name, other.name)
			&& price == other.price && Objects.equals(storage, other.storage) && Objects.equals(temp, other.temp)
			&& Objects.equals(timestamp, other.timestamp);
  }
}
