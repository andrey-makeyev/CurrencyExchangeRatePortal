package com.portal.exchangerate.controller;

import com.portal.exchangerate.dto.LastUpdateDTO;
import com.portal.exchangerate.schedule.DataUpdateScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    private final DataUpdateScheduler dataUpdateScheduler;

    @Autowired
    public HomeController(DataUpdateScheduler dataUpdateScheduler) {
        this.dataUpdateScheduler = dataUpdateScheduler;
    }

    @RequestMapping(value = "/{path:[^\\.]*}")
    public String redirect() {
        return "forward:/";
    }

    @GetMapping(value = "/api/last-update", produces = "application/json")
    @ResponseBody
    public EntityModel<LastUpdateDTO> getLastUpdate() {
        LastUpdateDTO lastUpdateDTO = new LastUpdateDTO(dataUpdateScheduler.getLastUpdate().atStartOfDay());
        EntityModel<LastUpdateDTO> resource = EntityModel.of(lastUpdateDTO);
        resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(HomeController.class).getLastUpdate()).withSelfRel());
        return resource;
    }

}