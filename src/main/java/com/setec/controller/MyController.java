package com.setec.controller;

import java.io.File;
import java.nio.file.Paths;
import java.security.Identity;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.setec.dao.PostProductDAO;
import com.setec.dao.PutProductDAO;
import com.setec.entities.Product;
import com.setec.repos.ProductRepo;



//http://localhost:8080/swagger-ui/index.html#/my-controller/getAllProduct




@RestController
@RequestMapping("/api/product")
public class MyController {
	
	@Autowired
	private ProductRepo productRepo;
	
	@GetMapping
	public Object getAllProduct() {
	 
	    var products = productRepo.findAll();
	    
		if(products.size()>0) {
			return products;
			
		}

		return ResponseEntity.status(404).body(Map.of("message","product is empty"));
		
		}
	    
	   @GetMapping("/name/{name}")
	   public Object getById(@PathVariable("id") String name){
		   List<Product> products = ProductRepo.findByName(name);
		   if(products.size()>0)
			   return products;
		   
		   
		   return ResponseEntity.status(404).body(Map.of("message","product name ="+name+"not found"));
		   
	   }
	   
	   @DeleteMapping("/{id}")
	   public Object deletedById(@PathVariable("id") Integer id) {
		   var product=productRepo.findById(id);
		   if(product.isPresent()) {
			   var pro=product.get();
			   new File("myApp/"+pro.getImageUrl()).delete();			   
			   productRepo.delete(product.get());
			   return ResponseEntity.status(202).body(Map.of("message","product name ="+id+"has been deleted "));
		   }
		   
		   return ResponseEntity.status(404).body(Map.of("message","product name ="+id+"not found"));
	   }
	   
		   
	   @GetMapping("/{id}")
	   public Object getById(@PathVariable("id") Integer id){
		   var product=productRepo.findById(id);
		   if(product.isPresent())
		      return product.get();
		   
		   return ResponseEntity.status(404).body(Map.of("message","Product id="+id+"not found"));
	   }
	    
	    
	    
	
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Object addProduct(@ModelAttribute PostProductDAO product)throws Exception{
		var file = product.getFile();
		String uploadDir = new File("myApp/static").getAbsolutePath();
		File dir = new File(uploadDir);
		if(!dir.exists()) {
		   dir.mkdirs();
		}
		String fileName = file.getOriginalFilename();
		String uniqueName = UUID.randomUUID()+"_"+fileName;
		String filePath = Paths.get(uploadDir,uniqueName).toString();
		
	    file.transferTo(new File(filePath));
	    
	    var pro = new Product();
	    pro.setName(product.getName());
	    pro.setPrice(product.getPrice());
	    pro.setQty(product.getQty());
	    pro.setImageUrl("/static/"+uniqueName);
	    productRepo.save(pro);
		
		return ResponseEntity.status(201).body(pro);
	}
	
	@PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Object updateProduct(@ModelAttribute PutProductDAO product)throws Exception{
		Integer id = product.getId();
		var p = productRepo.findById(id);
		if(p.isPresent()) {
			var update = p.get();
			update.setName(product.getName());
			update.setPrice(product.getPrice());
			update.setQty(product.getQty());
			
			if(product.getFile()!=null) {
				var file = product.getFile();
				String uploadDir = new File("myApp/static").getAbsolutePath();
				File dir = new File(uploadDir);
				if(!dir.exists()) {
				   dir.mkdirs();
				}
				
				String fileName = file.getOriginalFilename();
				String uniqueName = UUID.randomUUID()+"_"+fileName;
				String filePath = Paths.get(uploadDir,uniqueName).toString();
				new File("myApp/"+update.getImageUrl()).delete();
				
			    file.transferTo(new File(filePath));
			    
			    update.setImageUrl("/static/"+uniqueName);
			}
			
			
			productRepo.save(update);
			return ResponseEntity.status(202).body(update);
			
		}
		
		 return ResponseEntity.status(404).body(Map.of("message","product name ="+id+"not found"));
	}
	
	
}

