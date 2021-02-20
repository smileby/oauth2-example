package com.baiyun.controller;

import com.baiyun.model.Client;
import com.baiyun.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("client")
public class ClientController {


    @Autowired
    private ClientService clientService;

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String add(@ModelAttribute(value = "client")Client client, RedirectAttributes redirectAttributes){
        clientService.createClient(client);
        String clientName = client.getClientName();
        redirectAttributes.addFlashAttribute("msg", "新增成功： " + clientName);
        return "redirect:/page/managerClient";
    }

    @RequestMapping(value = "/delete/{id}")
    public String clientDelete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        clientService.deleteClient(id);
        redirectAttributes.addFlashAttribute("msg", "删除成功");
        return "redirect:/page/managerClient";
    }

    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    public String update(Client client, RedirectAttributes redirectAttributes) {
        clientService.updateClient(client);
        redirectAttributes.addFlashAttribute("msg", "修改成功");
        return "redirect:/page/managerClient";
    }
}
