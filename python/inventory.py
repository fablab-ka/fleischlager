#!/usr/bin/env python
# -*- coding: utf-8 -*-

import openerplib
from pprint import pprint

hostname = "erp.fablab-karlsruhe.de"
database = "FabLab_Karlsruhe"
username = "fleischlager"
password = "fleischlager"

class Inventory:

	def __init__(self, dbname, user, pwd):
		self.connection = openerplib.get_connection(hostname=hostname, database=database, login=user, password=pwd, port=80)
		self.product_model = self.connection.get_model("product.product")
		self.user_model = self.connection.get_model("res.users")
		self.stock_change_product_qty = self.connection.get_model("stock.change.product.qty")

		self.relevant_fields = {
			"product": [ 'name', 'default_code', 'type', 'qty_available', 'loc_case', 'loc_rack', 'loc_row', 'ean13', 'list_price' ],
			"user": [ 'name' ]
		}

	def searchUser(self, name):
		ids = self.user_model.search([("login", "=", name)])
		return ids

	def getUser(self, id):
		user_info = self.user_model.read(id, self.relevant_fields["user"])
		return user_info

	def searchProduct(self, description):
		ids = self.product_model.search([("name", "=", description)])
		return ids

	def getProduct(self, id):
		product_info = self.product_model.read(id, self.relevant_fields["product"])
		return product_info

	def getProducts(self):
		result = []
		ids = self.product_model.search([])
		for id in ids:
			product = self.getProduct(id)
			result.append(product)
		return result
	
	def changeProductQuantity(self, product_id, qty):
		data = {
			"product_id": product_id,
			"location_id": 1,
			"new_quantity": 2
		}
		self.stock_change_product_qty.change_product_qty(data)

	def addProduct(self, name, qty=1, case="", rack="", row=""):
		count = len(self.product_model.search([]))
		data = {
			'name': name, 
			'default_code': '%06d' % (count+1), 
			'type': 'product', 
			'qty_available': qty, 
			'loc_case': case,
			'loc_rack': rack,
			'loc_row': row,
			#'ean13': ,
			#'list_price': 0
		}
		product_id = self.product_model.create(data)
		if qty > 0:
			self.changeProductQuantity(product_id, qty)
			
		return product_id

#SAMPLE CODE
if False:
	print "init inventory connection"
	inventory = Inventory('FabLab_Karlsruhe', username, password)
	print "searching product"
	product_id = inventory.searchProduct("Gliedermaßstab")[0]
	print product_id
	print "changing qty"
	inventory.changeProductQuantity(product_id, 4)
	#products = inventory.getProducts()
	#pprint(products)
