package com.baiyun.controller;

import com.baiyun.model.Client;
import com.baiyun.model.User;
import com.baiyun.service.ClientService;
import com.baiyun.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("page")
public class PageController {


    @Autowired
    private ClientService clientService;
    @Autowired
    private UserService userService;

// =======================  客户端管理 start ==============================

    /**
     * 客户端管理界面
     * @return
     */
    @RequestMapping("/managerClient")
    public String managerClient(Model model){
        model.addAttribute("clientList", clientService.findAll());
        return "manager/client_list";
    }

    /**
     * 跳转新增客户端页面
     * @param model
     * @return
     */
    @RequestMapping("/clientAdd")
    public String clientAdd(Model model){
        model.addAttribute("client", new Client());
        model.addAttribute("buttonName", "新增");
        model.addAttribute("action_url", "/client/create");
        return "manager/client/edit";
    }

    @RequestMapping("/clientUpdate/{id}")
    public String clientUpdate(@PathVariable("id") Long id, Model model){
        model.addAttribute("client", clientService.findOne(id));
        model.addAttribute("buttonName", "修改");
        model.addAttribute("action_url", "/client/update/" + id);

        return "manager/client/edit";
    }

// =======================  注册用户管理 start ==============================

    /**
     * 用户管理界面
     * @return
     */
    @RequestMapping("/managerUser")
    public String managerUser(Model model){
        model.addAttribute("userList", userService.findAll());
        return "manager/user_list";
    }

    /**
     * 跳转新增用户界面
     * @param model
     * @return
     */
    @RequestMapping("/userAdd")
    public String userAdd(Model model){
        model.addAttribute("user", new User());
        model.addAttribute("buttonName", "新增");
        model.addAttribute("action_url", "/user/create");
        return "manager/user/edit";
    }

    /**
     * 用户修改
     * @param id
     * @param model
     * @return
     */
    @RequestMapping("/userUpdate/{id}")
    public String userUpdate(@PathVariable("id") Long id, Model model){
        model.addAttribute("user", userService.findOne(id));
        model.addAttribute("buttonName", "修改");
        model.addAttribute("action_url", "/user/update/" + id);
        return "manager/user/edit";
    }

    /**
     * 用户密码修改
     * @param id
     * @param model
     * @return
     */
    @RequestMapping(value = "/changePassword/{id}", method = RequestMethod.GET)
    public String showChangePasswordForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("user", userService.findOne(id));
        model.addAttribute("buttonName", "修改密码");
        model.addAttribute("action_url", "/user/changePassword/" + id);
        return "manager/user/changePassword";
    }
}
