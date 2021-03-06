package com.example.merchant.controller;

import com.example.merchant.Response.ResponseDto;
import com.example.merchant.Response.ResponseProductDto;
import com.example.merchant.dto.MerchantDto;
import com.example.merchant.dto.ProductDto;
import com.example.merchant.dto.RequestProductDto;
import com.example.merchant.entity.Merchant;
import com.example.merchant.entity.Product;
import com.example.merchant.service.MerchantService;
import com.example.merchant.service.ProductService;
import com.example.merchant.service.feignServices.ProductFeign;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping(value="/merchant")
public class MerchantController {

    @Autowired
    MerchantService merchantService;
    @Autowired
    ProductService productService;
    @Autowired
    ProductFeign productFeign;

    @GetMapping(value = "/{idFromMerchant}")
    MerchantDto select(@PathVariable(value="idFromMerchant") String id){
        Merchant merchant=merchantService.select(id);
        MerchantDto merchantDto=createDTOFromEntity(merchant);
        return merchantDto;
    }

    @GetMapping(value = "/profile/{id}")
    ResponseDto getProductsById(@PathVariable(value = "id") String id){
        Merchant merchant=merchantService.select(id);
        ResponseDto responseDto=new ResponseDto();
        responseDto.setName(merchant.getName());
        responseDto.setPoints(merchant.getPoints());
        responseDto.setTotal_sales(merchant.getTotal_sales());
        List<Product> products= productService.findByMerchantId(merchant.getId());
        List<ResponseProductDto> result=new ArrayList<>();
        for(int i=0;i<products.size();i++){
            ResponseProductDto temp=new ResponseProductDto();
            BeanUtils.copyProperties(products.get(i),temp);
            RequestProductDto rpd=new RequestProductDto();
            BeanUtils.copyProperties(productFeign.select(products.get(i).getProductId()),rpd);
            temp.setTitle(rpd.getTitle());
            temp.setImage(rpd.getImage());
            result.add(temp);
        }
        responseDto.setProducts(result);
        return responseDto;
    }

    @RequestMapping(method ={RequestMethod.POST,RequestMethod.PUT})
    void save(@RequestBody MerchantDto merchantDto)
    {
        Merchant merchant=createEntityFromDto(merchantDto);
        merchantService.save(merchant);
    }


    public Merchant createEntityFromDto(MerchantDto dto){

        Merchant merchant=new Merchant();
        BeanUtils.copyProperties(dto,merchant);

        return merchant;
    }

    public MerchantDto createDTOFromEntity(Merchant entity){
        MerchantDto merchantDto=new MerchantDto();
        BeanUtils.copyProperties(entity,merchantDto);
        return merchantDto;
    }

}
