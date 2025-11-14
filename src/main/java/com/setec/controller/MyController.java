package com.setec.controller;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.setec.WebProductApiApplication;
import com.setec.dao.PostProductDAO;
import com.setec.dao.PutProductDAO;
import com.setec.entities.Product;
import com.setec.repos.ProductRepo;

@RestController
@RequestMapping("/api/product")
public class MyController {

    private final WebProductApiApplication webProductApiApplication;
  //http://localhost:8080/swagger-ui/index.html
  
  @Autowired
  private ProductRepo productRpo;
  
  MyController(WebProductApiApplication webProductApiApplication){
    this.webProductApiApplication = webProductApiApplication;
  }
  @Controller
  public class HomeController {
      @GetMapping("/")
      public String redirectToSwagger() {
          return "redirect:/swagger-ui/index.html";
      }
  }
  @DeleteMapping("/{id}")
  public Object deletedById(@PathVariable("id") Integer id) {
    var product = productRpo.findById(id);
    if(product.isPresent()) {
      var pro = product.get();
      new File("myApp/"+pro.getImageUrl()).delete();
      productRpo.delete(product.get());
      return ResponseEntity.status(202).body(Map.of("message","Product id = "+ id +" deleted"));
    }
    return ResponseEntity.status(404).body(Map.of("message","Product id = "+ id +" not found"));
  }
  @GetMapping("/{id}")
  public Object getById(@PathVariable("id") Integer id) {
    
    var product = productRpo.findById(id);
    if(product.isPresent())
      return product.get();
    
    return ResponseEntity.status(404).body(Map.of("message","Product id ="+ id +"not found"));
  }
  
  @GetMapping("/name/{name}")
  public Object getById(@PathVariable("name") String name) {
    
    List<Product> products = productRpo.findByName(name);
    if(products.size()>0)
      return products;
    return ResponseEntity.status(404).body(Map.of("message","Product name ="+ name +"not found"));
    
  }
  
  @GetMapping
  public Object getAllProdcut() {
    var products = productRpo.findAll();
    if(products.size()>0) {
      
      return ResponseEntity.status(201).body(products);
    }
    return ResponseEntity.status(404).body(products);
  }
  
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Object addProduct(@ModelAttribute PostProductDAO product)throws Exception { 
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
    
    productRpo.save(pro);
    return ResponseEntity.status(201).body(pro);
  }
  @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Object updateProduct(@ModelAttribute PutProductDAO product)throws Exception { 
    Integer id = product.getId();
    var p = productRpo.findById(id);
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
      productRpo.save(update);
      return ResponseEntity.status(202).body(update);
    }
    return ResponseEntity.status(404).body(Map.of("message","Product id = "+ id +" not found"));
  }
  
}
      