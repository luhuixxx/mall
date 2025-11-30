package com.luxiao.malluser.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luxiao.mallmodel.user.Employee;
import com.luxiao.malluser.dto.EmployeeLoginReq;
import com.luxiao.malluser.dto.EmployeeLoginResp;
import com.luxiao.malluser.dto.EmployeeRegisterReq;
import com.luxiao.malluser.dto.EmployeeUpdateReq;

public interface EmployeeService extends IService<Employee> {
    EmployeeLoginResp login(EmployeeLoginReq req);
    Page<Employee> pageEmployees(int page, int size, String username);
    Employee register(EmployeeRegisterReq req);
    Employee updateProfile(Long id, EmployeeUpdateReq req);
}
