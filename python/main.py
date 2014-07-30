#!/usr/bin/env python
# -*- coding: utf-8 -*-

from inventory import Inventory
import pygame

class Main:
	def __init__(self):
		self.backgroundcolor = (255,255,255)
		
		self.textCache = {}
		self.running = False
		
		self.data_update_interval = 5000
		self.last_data_update = 0
		
		self.product_list = []
		
		pygame.init()
		
		self.fullscreen_dimensions = (pygame.display.Info().current_w, pygame.display.Info().current_h)
		
		self.screen = pygame.display.set_mode(self.fullscreen_dimensions, pygame.RESIZABLE)
		
		self.font_arial = pygame.font.SysFont( 'arial,courier', 14 )
		testRendering = self.get_text(self.font_arial, "test")
		self.font_arial_height = testRendering.get_size()[0]
		
		self.inventory = Inventory('FabLab_Karlsruhe', "fleischlager", "fleischlager")
	
	def get_text(self, font, text, color=(0,0,0)):
		key = str(self.font_arial) + text
		
		if not self.textCache.has_key(key):
			self.textCache[key] = font.render(text, True, color)
		
		return self.textCache[key]
	
	def draw_text(self, font, text, pos):
		text_surface = self.get_text(font, text)
		self.screen.blit( text_surface, pos )
		
	def run(self):
		self.running = True
		
		self.update_data()
		
		clock = pygame.time.Clock()
		
		while self.running:
			clock.tick(60)
			
			self.poll()
			self.update()
			self.draw()
			
			pygame.display.update()
	
	def poll(self):
		for event in pygame.event.get() :
		  if event.type == pygame.KEYUP :
			if event.key == pygame.K_ESCAPE :
			  self.running = False
	
	def update(self):
		if (pygame.time.get_ticks() - self.last_data_update) > self.data_update_interval:
			self.update_data()
	
	def update_data(self):
		self.product_list = self.inventory.getProducts()
	
	def draw(self):
		self.screen.fill( self.backgroundcolor )
		
		for i in range(len(self.product_list)):
			p = self.product_list[i]
			if p["type"] == "product":
				self.draw_text(self.font_arial, p["default_code"] + " - " + p["name"], (10, 10 + self.font_arial_height * i) )


if __name__ == "__main__":
	main = Main()
	
	print " === Starting Fleischlager Client === "
	main.run()
	print " === Stopping Fleischlager Client === "
