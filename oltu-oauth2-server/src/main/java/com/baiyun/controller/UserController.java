package com.baiyun.controller;

import com.baiyun.model.Client;
import com.baiyun.model.User;
import com.baiyun.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 修改用户信息
     * @param user
     * @param redirectAttributes
     * @return
     */
    @RequestMapping(value = "/update/{id}")
    public String updateUser(User user, RedirectAttributes redirectAttributes){
        userService.updateUser(user);
        redirectAttributes.addFlashAttribute("msg", "修改成功");
        return "redirect:/page/managerUser";
    }

    @RequestMapping(value = "/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("msg", "删除成功");
        return "redirect:/page/managerUser";
    }

    @RequestMapping(value = "/create")
    public String add(@ModelAttribute(value = "user")User user, RedirectAttributes redirectAttributes){
        userService.createUser(user);
        String userName = user.getUsername();
        redirectAttributes.addFlashAttribute("msg", "新增成功： " + userName);
        return "redirect:/page/managerUser";
    }

    @RequestMapping(value = "/changePassword/{id}")
    public String changePassword(@PathVariable("id") Long id, String newPassword, RedirectAttributes redirectAttributes) {
        userService.changePassword(id, newPassword);
        redirectAttributes.addFlashAttribute("msg", "修改密码成功");
        return "redirect:/page/managerUser";
    }
}
