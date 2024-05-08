package com.fitness.fitness.controller;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fitness.fitness.model.Appointment;
import com.fitness.fitness.model.FitnessClass;
import com.fitness.fitness.model.Trainer;
import com.fitness.fitness.model.User;
import com.fitness.fitness.service.AppointmentService;
import com.fitness.fitness.service.FitnessClassService;
import com.fitness.fitness.service.PlanService;
import com.fitness.fitness.service.TrainerService;
import com.fitness.fitness.service.UserService;
import org.springframework.web.bind.annotation.RequestParam;




@Controller
public class AppointmentController {

    private final UserService userService;
    private final AppointmentService appointmentService;
    private final TrainerService trainerService;
    private final FitnessClassService fitnessClassService;
    private final PlanService planService;

    @Autowired
    public AppointmentController(UserService userService, AppointmentService appointmentService,  TrainerService trainerService, FitnessClassService fitnessClassService,  PlanService planService) {
        this.userService = userService;
        this.appointmentService = appointmentService;
        this.trainerService = trainerService;
        this.fitnessClassService = fitnessClassService;
        this.planService = planService;
        
    }
    

    @GetMapping("/userAppointment")
    public String userAppointment(Model model, @SessionAttribute("user") User user) {
        // Update the status of past appointments to "inactive"
        appointmentService.deactivatePastAppointments();
        List<FitnessClass> classList = fitnessClassService.getAllClasses();
        List<Trainer> trainerList =trainerService.getAllTrainers();
    
        User retrievedUser = userService.getUserByEmail(user.getEmail());
        model.addAttribute("retrievedUser", retrievedUser); // Add retrievedUser to the model as an attribute
        int userId = retrievedUser.getUserId(); // ID from user email
    
        List<Integer> appointmentIds = appointmentService.getAppointmentIdsByUserId(userId);
        model.addAttribute("appointmentIds", appointmentIds);
        model.addAttribute("classList", classList);
        model.addAttribute("trainerList", trainerList);
    
        return "userViewAppointments"; 
    }



    public String getTrainerNameByAppointmentId(int appointmentId) {
        return appointmentService.getTrainerNameByAppointmentId(appointmentId);
    }
    public String getClassNameByAppointmentId(int appointmentId) {
        return appointmentService.getClassNameByAppointmentId(appointmentId);
    }
    public LocalDateTime getDateTimeByAppointmentId(int appointmentId){
        return appointmentService.getDateTimeByAppointmentId(appointmentId);
    }

    public String getStatusByAppointmentId(int appointmentId){
        return appointmentService.getStatusByAppointmentId(appointmentId);
    }

    public int getUserIdByAppointmentId(int appointmentId){
        return appointmentService.getUserIdByAppointmentId(appointmentId);
    }

    
    
    @GetMapping("/filterAppointments")
    public String filterAppointments(Model model, 
                                    @SessionAttribute("user") User user,
                                    @RequestParam(name = "classId", required = false) Integer classId,
                                    @RequestParam(name = "trainerId", required = false) Integer trainerId,
                                    @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                    @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;
        User retrievedUser = userService.getUserByEmail(user.getEmail());
        model.addAttribute("retrievedUser", retrievedUser); // Add retrievedUser to the model as an attribute
        int userId = retrievedUser.getUserId(); // ID from user email
    
        List<Appointment> filteredAppointments = appointmentService.findAppointmentsByFilters(
        userId, classId, trainerId, startDateTime, endDateTime);
        model.addAttribute("appointments", filteredAppointments);

        return "filterAppointments";
                                    }
    
    
    

    @GetMapping("/book_appointment")
    public String bookAppointment(Model model, 
                              @RequestParam(name = "classId", required = false) Integer classId,
                              @RequestParam(name = "className", required = false) String className,
                              @SessionAttribute("user") User user) {
        try {
        //this is for if you choose book appointment from trainer or home page
            if (classId == null) {
                List<FitnessClass> classList = fitnessClassService.getAllClasses();
        User retrievedUser = userService.getUserByEmail(user.getEmail());
        model.addAttribute("retrievedUser", retrievedUser); // Add retrievedUser to the model as an attribute
        int planTypeId = planService.findByPlanType(retrievedUser.getStatus()).getId();
        List<Trainer> trainerList = trainerService.getTrainersByPlanId(planTypeId);


        // Formatting date and time for the frontend
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        String formattedDateTimeNow = now.format(formatter);
        LocalDateTime Activenow = LocalDateTime.now();
        DateTimeFormatter Activeformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDate activeDate = user.getActiveDate(); // Assuming this is a LocalDate
        LocalDateTime endOfActiveDate = activeDate.atTime(23, 59); // End of the active date
        
        model.addAttribute("formattedDateTimeNow", Activenow.format(Activeformatter));
        model.addAttribute("endOfActiveDate", endOfActiveDate.format(formatter)); // Pass as LocalDateTime formatted 
        model.addAttribute("classList", classList);
        model.addAttribute("trainerList", trainerList);
        model.addAttribute("formattedDateTimeNow", formattedDateTimeNow);
            } else {
        // if you choose book appointment from view class
        List<FitnessClass> classList = fitnessClassService.getAllClasses();
        User retrievedUser = userService.getUserByEmail(user.getEmail());
        model.addAttribute("retrievedUser", retrievedUser); // Add retrievedUser to the model as an attribute
        int planTypeId = planService.findByPlanType(retrievedUser.getStatus()).getId();
        List<Trainer> trainerList = trainerService.getTrainersByPlanId(planTypeId);


        // Formatting date and time for the frontend
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        String formattedDateTimeNow = now.format(formatter);
        LocalDateTime Activenow = LocalDateTime.now();
        DateTimeFormatter Activeformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDate activeDate = user.getActiveDate(); // Assuming this is a LocalDate
        LocalDateTime endOfActiveDate = activeDate.atTime(23, 59); // End of the active date
        // Fetch the selected class by ID
        FitnessClass fitnessClass = fitnessClassService.getClassById(classId);
        model.addAttribute("classId", classId);
        // Add the selected class to the model
        model.addAttribute("fitnessClass", fitnessClass);
        // Pass the class name as a model attribute
        model.addAttribute("className", className);
        model.addAttribute("formattedDateTimeNow", Activenow.format(Activeformatter));
        model.addAttribute("endOfActiveDate", endOfActiveDate.format(formatter)); // Pass as LocalDateTime formatted 
        model.addAttribute("classList", classList);
        model.addAttribute("trainerList", trainerList);
        model.addAttribute("formattedDateTimeNow", formattedDateTimeNow);
            }

        return "bookAppointment";}
        catch (Exception e) {
            // If any exception occurs, it will be caught here and redirected to the error page
            return "errorStatus";
        }
    }

    @PostMapping("/book_appointment")
public String bookAppointment(@ModelAttribute Appointment appointment, 
                              @SessionAttribute("user") User user,
                              @RequestParam("classId") int classId,
                              @RequestParam("trainerId") int trainerId,
                              @RequestParam("datetime") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime datetime,
                              Model model) {

    // Fetch the current date and time
    LocalDateTime currentDateTime = LocalDateTime.now();

    // Check if the selected datetime is at least one hour ahead of the current time if it's on the same day
    if (datetime.isBefore(currentDateTime.plusHours(1)) && datetime.toLocalDate().isEqual(currentDateTime.toLocalDate())) {
        // If the selected datetime is not valid, add an error message to the model and return to the form
        model.addAttribute("errorMessage", "Please select a time at least one hour ahead of the current time.");

        // Repopulate the model with other necessary attributes
        List<FitnessClass> classList = fitnessClassService.getAllClasses();
        User retrievedUser = userService.getUserByEmail(user.getEmail());
        model.addAttribute("retrievedUser", retrievedUser); // Add retrievedUser to the model as an attribute
        int planTypeId = planService.findByPlanType(retrievedUser.getStatus()).getId();
        List<Trainer> trainerList = trainerService.getTrainersByPlanId(planTypeId);
        
        // Formatting date and time for the frontend
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        String formattedDateTimeNow = now.format(formatter);
        LocalDate activeDate = user.getActiveDate(); // Assuming this is a LocalDate
        LocalDateTime endOfActiveDate = activeDate.atTime(23, 59); // End of the active date

        model.addAttribute("classList", classList);
        model.addAttribute("trainerList", trainerList);
        model.addAttribute("formattedDateTimeNow", formattedDateTimeNow);
        model.addAttribute("endOfActiveDate", endOfActiveDate.format(formatter));

        // Return to the form page
        return "bookAppointment"; 
    }

    // Continue with appointment booking logic if the datetime is valid

    // Fetch the related FitnessClass and Trainer objects based on the IDs provided
    FitnessClass fitnessClass = fitnessClassService.getClassById(classId);
    Trainer trainer = trainerService.getTrainerById(trainerId);

    // Set the fetched entities to the appointment
    appointment.setFitnessClass(fitnessClass);
    appointment.setTrainer(trainer);

    // Set the user from session to the appointment
    User retrievedUser = userService.getUserByEmail(user.getEmail());
    appointment.setUser(retrievedUser);

    // Set the date and time for the appointment
    appointment.setDate(datetime);

    // Set the status of the appointment to "active"
    appointment.setStatus("active");

    // Save the appointment to the database
    appointmentService.saveAppointment(appointment);

    return "redirect:/userAppointment"; // Redirect to a confirmation or listing page
}



    @GetMapping("/edit_appointment_page")
    public String editAppointmentPage(@RequestParam("appointmentId") int appointmentId, Model model, @SessionAttribute("user") User user) {
        try{
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);
        List<FitnessClass> classList = fitnessClassService.getAllClasses();

        User retrievedUser = userService.getUserByEmail(user.getEmail());
        model.addAttribute("retrievedUser", retrievedUser); // Add retrievedUser to the model as an attribute
        int userId = retrievedUser.getUserId(); // ID from user email
        int planTypeId = planService.findByPlanType(retrievedUser.getStatus()).getId();
        List<Trainer> trainerList = trainerService.getTrainersByPlanId(planTypeId);


        // Formatting date and time for the frontend
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        String formattedDateTimeNow = now.format(formatter);
        String formattedAppointmentDateTime = appointment.getDate().format(formatter);
        
        appointment.setStatus("active");
        model.addAttribute("userId", userId);
        model.addAttribute("appointment", appointment);
        model.addAttribute("classList", classList);
        model.addAttribute("trainerList", trainerList);
        model.addAttribute("status", appointment.getStatus());
        model.addAttribute("formattedDateTimeNow", formattedDateTimeNow);
        model.addAttribute("formattedAppointmentDateTime", formattedAppointmentDateTime);
        
        return "editAppointment";}

        catch (Exception e) {
            // If any exception occurs, it will be caught here and redirected to the error page
            return "errorStatus";
        }
    }
    

    @PostMapping("/save_appointment")
    public String saveAppointment(@ModelAttribute Appointment appointment, 
                                  @SessionAttribute("user") User user,
                                  @RequestParam("classId") int classId,
                                  @RequestParam("trainerId") int trainerId,
                                  @RequestParam("datetime") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime datetime,
                                  Model model) {
    
        // Fetch the current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();
    
        // Check if the selected datetime is at least one hour ahead of the current time if it's on the same day
        if (datetime.isBefore(currentDateTime.plusHours(1)) && datetime.toLocalDate().isEqual(currentDateTime.toLocalDate())) {
            // If the selected datetime is not valid, add an error message to the model and return to the form
            model.addAttribute("errorMessage", "Please select a time at least one hour ahead of the current time.");
    
            // Repopulate the model with other necessary attributes
            List<FitnessClass> classList = fitnessClassService.getAllClasses();
            User retrievedUser = userService.getUserByEmail(user.getEmail());
            model.addAttribute("retrievedUser", retrievedUser); // Add retrievedUser to the model as an attribute
            int planTypeId = planService.findByPlanType(retrievedUser.getStatus()).getId();
            List<Trainer> trainerList = trainerService.getTrainersByPlanId(planTypeId);
            
            // Formatting date and time for the frontend
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            String formattedDateTimeNow = now.format(formatter);
            LocalDate activeDate = user.getActiveDate(); // Assuming this is a LocalDate
            LocalDateTime endOfActiveDate = activeDate.atTime(23, 59); // End of the active date

            appointment.setStatus("active");
            model.addAttribute("classList", classList);
            model.addAttribute("trainerList", trainerList);
            model.addAttribute("formattedDateTimeNow", formattedDateTimeNow);
            model.addAttribute("endOfActiveDate", endOfActiveDate.format(formatter));

    
            // Return to the form page
            return "editAppointment"; 
        }
    
        // Continue with appointment saving logic if the datetime is valid
    
        // Fetch the related FitnessClass and Trainer objects based on the IDs provided
        FitnessClass fitnessClass = fitnessClassService.getClassById(classId);
        Trainer trainer = trainerService.getTrainerById(trainerId);
    
        // Set the fetched entities to the appointment
        appointment.setFitnessClass(fitnessClass);
        appointment.setTrainer(trainer);
    
        // Set the user from session to the appointment
        User retrievedUser = userService.getUserByEmail(user.getEmail());
        appointment.setUser(retrievedUser);
    
        // Set the date and time for the appointment
        appointment.setDate(datetime);
    
        // Save the appointment to the database
        appointmentService.updateAppointment(appointment);
    
        return "redirect:/userAppointment"; // Redirect to a confirmation or listing page
    }
    

    @PostMapping("/cancel_appointment")
    public String cancelAppointment(@RequestParam("appointmentId") int appointmentId) {
        // Retrieve the appointment by ID
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);
        
        // Check if the appointment status is "active"
        if (appointment.getStatus().equals("active")) {
            // Update the status to "inactive"
            appointment.setStatus("inactive");
            // Save the updated appointment
            appointmentService.updateAppointment(appointment);
        }
        
        // Redirect to the user's appointment page
        return "redirect:/userAppointment";
    }



    
    // // Method to fetch trainer ID by appointment ID for Thymeleaf template JGN DI HAPUS KERJA KERAS 6 JAM
    // public int getTrainerIdByAppointmentId(int appointmentId) {
    //     return appointmentService.getTrainerIdByAppointmentId(appointmentId);
    // }

}
