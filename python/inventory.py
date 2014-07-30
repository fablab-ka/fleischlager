import openerplib
from pprint import pprint

hostname = "erp.fablab-karlsruhe.de"
database = "FabLab_Karlsruhe"


class Inventory:

	def __init__(self, dbname, user, pwd):
		self.connection = openerplib.get_connection(hostname=hostname, database=database, login=user, password=pwd, port=80)
		self.product_model = self.connection.get_model("product.product")
		self.user_model = self.connection.get_model("res.users")

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
		ids = self.product_model.search([("description", "=", description)])
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
		return product_id

#SAMPLE CODE
if False:
	print "init inventory connection"
	inventory = Inventory('FabLab_Karlsruhe', '<INSERT-USERNAME-HERE>', '<INSERT-PASSWORD-HERE>')
	print "searching users"
	print inventory.searchUser("admin")
	print "adding products"
	inventory.addProduct('Digitalmultimerter MM 31')
	products = inventory.getProducts()
	pprint(products)