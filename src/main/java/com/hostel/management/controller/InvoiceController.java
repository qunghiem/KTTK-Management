package com.hostel.management.controller;

import com.hostel.management.model.Invoice;
import com.hostel.management.model.Room;
import com.hostel.management.model.User;
import com.hostel.management.model.UtilityReading;
import com.hostel.management.repository.InvoiceRepository;
import com.hostel.management.service.InvoiceService;
import com.hostel.management.service.RoomService;
import com.hostel.management.service.UtilityService;
import com.hostel.management.repository.UtilityReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UtilityService utilityService;

    @Autowired
    private UtilityReadingRepository utilityReadingRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;
}