package com.qa.ims.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.qa.ims.persistence.dao.CustomerDAO;
import com.qa.ims.persistence.dao.ItemDAO;
import com.qa.ims.persistence.dao.OrderDAO;
import com.qa.ims.persistence.domain.Customer;
import com.qa.ims.persistence.domain.Item;
import com.qa.ims.persistence.domain.Order;
import com.qa.ims.utils.Utils;

/**
 * Takes in order details for CRUD functionality
 *
 */
public class OrderController implements CrudController<Order> {

    public static final Logger LOGGER = LogManager.getLogger();

	private OrderDAO orderDAO;
	private ItemDAO itemDAO;
	private CustomerDAO customerDAO;
	private Utils utils;
	
	public OrderController(CustomerDAO customerDAO, ItemDAO itemDAO, OrderDAO orderDAO, Utils utils) {
		super();
		this.customerDAO = customerDAO;
		this.itemDAO = itemDAO;
		this.orderDAO = orderDAO;
		this.utils = utils;
	}

	/**
	 * Reads all orders to the logger
	 * 
	 * @return List of Orders
	 */
	@Override
	public List<Order> readAll() {
		List<Order> orders = orderDAO.readAll();
		for (Order order : orders) {
			LOGGER.info(order.toString());
		}
		return orders;
	}

	/**
	 * Creates an Order by taking in user input
	 * 
	 * @return a new Order
	 */
	@Override
	public Order create() {
		
		Customer customer = null;
		
		do {		
			LOGGER.info("Please enter a customer id");
			Long customer_id = utils.getLong();
			customer = customerDAO.readCustomer(customer_id);
		} while (customer == null);
		
		
		HashMap<Item, Long> itemMap = new HashMap<>();
		String itemString;
		String[] itemArray;
		
		do {
			
			itemMap.clear();
			
			LOGGER.info("Please enter an item list [<item>:<quantity>,<item>:<quantity>...]");
			itemString = utils.getString();
			itemArray = itemString.split(",");
			
			if (itemString.matches("^([0-9]+:[0-9]+)+(,[0-9]+:[0-9]+)*$")) {
				
				Arrays.stream(itemArray)
				.map(s -> s.split(":"))
				.takeWhile(s -> itemDAO.readItem(Long.parseLong(s[0].trim())) != null)	// breaks if item is not in database
				.forEach(s -> itemMap.put(new Item(Long.parseLong(s[0].trim())),
						Long.parseLong(s[1].trim())));
				
			}
			
		} while (itemMap.size() != itemArray.length); // continues until all items in list are added
		
		Order order = orderDAO.create(new Order(customer, itemMap));
		LOGGER.info("Order created");
		return order;
	}

	/**
	 * Updates an existing Order by taking in user input
	 * 
	 * @return The updated Order
	 */
	@Override
	public Order update() {
		
		Long orderId = null;
		Order order = null;
		
		do {	
			LOGGER.info("Please enter the id of the order you would like to update");
			orderId = utils.getLong();
			order = orderDAO.readOrder(orderId);
		} while (order == null);
		
		System.out.println(order.toString());
		
		boolean isAction = false;
		
		do {
			
			LOGGER.info("What would you like to do with this order?");
			
			LOGGER.info("ADD ITEM");
			LOGGER.info("DELETE ITEM");
			LOGGER.info("UPDATE CUSTOMER");
			
			String input = utils.getString();
			
			Item item = null;
			Long itemId;
			Customer customer = null;
			Long customerId;
			
			switch(input.toUpperCase()) {
			
			case "ADD ITEM":
				isAction = true;
				do {	
					LOGGER.info("Please enter the id of the item you wish to add");
					itemId = utils.getLong();
					item = itemDAO.readItem(itemId);
				} while (item == null);	
				
				LOGGER.info("Please enter the quantity of the item you wish to add");
				Long quantity = utils.getLong();
				orderDAO.addLine(orderId, itemId, quantity);
				LOGGER.info("Item added");
				break;
				
			case "DELETE ITEM":
				isAction = true;
				do {	
					LOGGER.info("Please enter the id of the item you wish to delete");
					itemId = utils.getLong();
					item = itemDAO.readItem(itemId);
				} while (item == null);	
				orderDAO.deleteLine(orderId, itemId);
				LOGGER.info("Item deleted");
				break;
				
			case "UPDATE CUSTOMER":
				isAction = true;
				do {	
					LOGGER.info("Please enter the new customer id");
					customerId = utils.getLong();
					customer = customerDAO.readCustomer(customerId);
				} while (customer == null);	
				orderDAO.update(new Order(orderId, customer));
				LOGGER.info("Customer updated");
				break;
				
			default:
				break;
			
			}
			
		} while (!isAction);
		
		return order;
	}
	
	/**
	 * Deletes an existing order by the id of the order
	 * 
	 * @return number of orders deleted
	 */
	@Override
	public int delete() {
		LOGGER.info("Please enter the id of the order you would like to delete");
		Long id = utils.getLong();
		return orderDAO.delete(id);
	}

}