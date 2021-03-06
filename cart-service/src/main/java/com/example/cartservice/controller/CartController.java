package com.example.cartservice.controller;

import com.example.cartservice.Request.RequestDto;
import com.example.cartservice.dto.CartDto;
import com.example.cartservice.dto.MerchantDto;
import com.example.cartservice.dto.Product;
import com.example.cartservice.dto.ProductDto;
import com.example.cartservice.response.ResponseDto;
import com.example.cartservice.entity.Cart;
import com.example.cartservice.response.ResponseProductDto;
import com.example.cartservice.service.CartService;
import com.example.cartservice.service.feignServices.MerchantFeign;
import com.example.cartservice.service.feignServices.ProductFeign;
import com.example.cartservice.service.feignServices.UserFeign;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
@RestController
@RequestMapping(value="/cart")
public class CartController {

    @Autowired
    CartService cartService;

    @Autowired
    ProductFeign productFeign;

    @Autowired
    MerchantFeign merchantFeign;
    @Autowired
    UserFeign userFeign;

    @RequestMapping(method={RequestMethod.POST,RequestMethod.PUT})
    void save(@RequestBody CartDto cartDto) {
        cartService.save(copyDtoToEntity(cartDto));
    }


    @GetMapping(value="/id/{id}")
    CartDto getCartById(@PathVariable("id") String id){
        Cart cart=cartService.findById(id);
        return copyEntityToDto(cart);
    }

    @GetMapping(value="/email/{email}")
    ResponseDto getCartByEmail(@PathVariable(value="email") String email){
        Cart cart=cartService.findByEmail(email);
        CartDto cartDto = copyEntityToDto(cart);
        ResponseDto response=new ResponseDto();
        List<ResponseProductDto> list = new ArrayList<>();
        for(int i=0;i<cartDto.getProductList().size();i++){
            Product product = cartDto.getProductList().get(i);
            ProductDto productDto = productFeign.select(product.getProductId());
            MerchantDto merchantDto = merchantFeign.select(product.getMerchantId());
            ResponseProductDto productResponse=new ResponseProductDto(productDto.getTitle(), product.getPrice(),productDto.getCategory(),product.getQuantity(),productDto.getImage(),merchantDto.getName(), cartDto.getEmail(),merchantDto.getPoints(),merchantDto.getTotal_sales(),merchantDto.getId(),productDto.getId(),i);
            list.add(productResponse);
        }
        response.setEmail(cart.getEmail());
        response.setId(cart.getId());
        response.setProductList(list);
        return response;
    }

    @PostMapping("/{email}/inc")
    void addProduct(@PathVariable(value="email") String email, @RequestBody RequestDto requestDto){
        Cart cart=cartService.findByEmail(email);
        List<Product> result=new ArrayList<>();
        boolean flag=false;
        if (cart.getProductList().size()>0){
        for (Product prod :cart.getProductList()){
            if(prod.getProductId().equals(requestDto.getProductId())){
                Product product=new Product(prod.getProductId(),prod.getQuantity()+1,prod.getMerchantId(),prod.getPrice());
                flag=true;
                result.add(product);
            }else {
                Product product=new Product(prod.getProductId(),1,prod.getMerchantId(),prod.getPrice());
                result.add(product);
            }
        }
        if (!flag){
            Product product=new Product(requestDto.getProductId(),1,requestDto.getMerchantId(),requestDto.getPrice());
            result.add(product);
        }
        }else{
            Product product=new Product(requestDto.getProductId(),1,requestDto.getMerchantId(),requestDto.getPrice());
            result.add(product);
        }
        cart.setProductList(result);
        cartService.save(cart);
    }

    @PostMapping("/{email}/{id}/dec")
    void deleteProduct(@PathVariable(value="email") String email,@PathVariable(value = "id") String id){
        Cart cart=cartService.findByEmail(email);
        for (Product prod :cart.getProductList()){
            if(prod.getProductId().equals(id)){
                if(prod.getQuantity()==1){
                    cart.getProductList().remove(prod);
                }else{
                prod.setQuantity(prod.getQuantity()-1);
                }
                break;
            }
        }
        cartService.save(cart);
    }

    @PostMapping("/clear")
    void clearCart(@RequestBody String id){
        Cart cart = cartService.findByEmail(userFeign.getUser(id).getEmail());
        cart.setProductList(new ArrayList<>());
        cartService.save(cart);
    }

    private Cart copyDtoToEntity(CartDto cartDto){
        Cart cart = new Cart();
        BeanUtils.copyProperties(cartDto, cart);
        return cart;
    }

    private CartDto copyEntityToDto(Cart cart){
        CartDto cartDto = new CartDto();
        BeanUtils.copyProperties(cart, cartDto);
        return cartDto;
    }
}
