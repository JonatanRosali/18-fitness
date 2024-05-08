package com.fitness.fitness.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.fitness.fitness.model.FitnessClass;
import com.fitness.fitness.model.User;
import com.fitness.fitness.service.FitnessClassService;

import com.fitness.FileUploadUtil;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fitness.fitness.model.Income;
import com.fitness.fitness.model.Manager;
import com.fitness.fitness.model.PaymentTransaction;
import com.fitness.fitness.model.Appointment;
import com.fitness.fitness.model.Expense;
import com.fitness.fitness.model.FitnessClass;
import com.fitness.fitness.model.Manager;
import com.fitness.fitness.model.Plan;
import com.fitness.fitness.model.Review;
import com.fitness.fitness.model.Trainer;
import com.fitness.fitness.model.User;
import com.fitness.fitness.repository.ExpenseRepo;
import com.fitness.fitness.repository.IncomeRepo;
import com.fitness.fitness.repository.PaymentTransactionRepo;
import com.fitness.fitness.repository.PlanRepo;
import com.fitness.fitness.repository.TrainerRepo;
import com.fitness.fitness.repository.UserRepo;
import com.fitness.fitness.service.AppointmentService;
import com.fitness.fitness.service.EmailService;
import com.fitness.fitness.service.ManagerService;
import com.fitness.fitness.service.PlanService;
import com.fitness.fitness.service.ReviewService;
import com.fitness.fitness.service.TrainerService;
import com.fitness.fitness.service.UserService;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class ManagerController {
    @Autowired
    private EmailService emailService;

    @Autowired
    private ManagerService managerService;

    @Autowired
    private UserService userService;

    @Autowired
    private TrainerRepo trainerRepo;

    @Autowired
    private TrainerService trainerService;
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PaymentTransactionRepo paymentTransactionRepo;

    @Autowired
    private IncomeRepo incomeRepo;

    @Autowired
    private ReviewService reviewService;

    // cb cek ulang ini
    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PlanService planService;

    @Autowired
    private PlanRepo planRepo;

    @Autowired
    private ExpenseRepo expenseRepo;

    @Autowired
    private FitnessClassService fitnessClassService;

    @GetMapping("/manager_home_page")
    public String getHomePage(Model model, @SessionAttribute("manager") Manager manager) {
        if (manager != null) {
            model.addAttribute("manager", manager);
            return "manager_home";
        } else {
            return "redirect:/manager_signin";
        }
    }

    @GetMapping("/manager_signin")
    public String showLoginForm(Model model, HttpSession session) {
        session.invalidate();
        Manager existingManager = new Manager();
        model.addAttribute("manager", existingManager);
        return "sign_manager";
    }

    @PostMapping("/manager_signin")
    public String login(@ModelAttribute Manager manager, HttpSession session) {
        if (managerService.managerLogin(manager)) {
            session.setAttribute("manager", manager);
            return "redirect:/manager_home_page";
        }
        return "login_fail_manager";
    }

    @GetMapping("/managerViewTrainers")
    public String showTrainers(Model model, @SessionAttribute("manager") Manager manager) {
        model.addAttribute("trainers", trainerService.getAllTrainers());
        return "managerViewTrainers";
    }

    @GetMapping("/managerAddTrainer")
    public String showAddTrainerForm(Model model, @SessionAttribute("manager") Manager manager) {
        model.addAttribute("trainer", new Trainer());
        return "managerAddTrainer";
    }

    @PostMapping("/managerSaveTrainer")
    public String saveTrainer(@RequestParam("image") MultipartFile multipartFile,
            @ModelAttribute("trainer") Trainer trainer, Model model,
            @SessionAttribute("manager") Manager manager) throws IOException {
        trainerService.saveTrainer(trainer);
        planRepo.findAll().forEach(plan -> {
            Set<Trainer> trainers = plan.getTrainers();
            switch (plan.getPlanType()) {
                case "Silver":
                    if (trainer.getRank() <= 3) {
                        trainers.add(trainer);
                    }
                    break;
                case "Gold":
                    if (trainer.getRank() <= 4) {
                        trainers.add(trainer);
                    }
                    break;
                case "Diamond":
                    if (trainer.getRank() <= 5) {
                        trainers.add(trainer);
                    }
                    break;
            }
            plan.setTrainers(trainers);
            planRepo.save(plan);
        });

        if (!multipartFile.isEmpty()) {
            String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
            trainer.setPhoto(fileName);
            String upload = "src/main/resources/static/images/"; // Adjust this URL as needed
            FileUploadUtil.saveFile(upload, fileName, multipartFile);

        } else {
            if (trainer.getPhoto().isEmpty()) {
                trainer.setPhoto("wechat_icon.jpg");
                trainerService.updateTrainer(trainer);
            }
        }
        trainerService.saveTrainer(trainer);
        return "redirect:/managerViewTrainers";
    }

    @GetMapping("/editTrainer/{id}")
    public String editTrainer(@PathVariable("id") int id, Model model) {
        // Fetch the trainer from the database by id
        Trainer trainer = trainerService.getTrainerById(id);
        model.addAttribute("trainer", trainer);
        return "editTrainer";
    }

    @PostMapping("/updateTrainer")
    public String updateTrainer(@RequestParam("image") MultipartFile multipartFile, @ModelAttribute Trainer trainer,
            Model model) throws IOException {
        if (!multipartFile.isEmpty()) {
            String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
            trainer.setPhoto(fileName);
            String upload = "src/main/resources/static/images/"; // Adjust this URL as needed
            FileUploadUtil.saveFile(upload, fileName, multipartFile);

        } else {
            if (trainer.getPhoto().isEmpty()) {
                trainer.setPhoto("wechat_icon.jpg");
                trainerService.updateTrainer(trainer);
            }
        }
        trainerService.saveTrainer(trainer);
        trainerService.updateTrainer(trainer);
        return "redirect:/managerViewTrainers";
    }

    @PostMapping("/removeTrainer/{id}")
    public String removeTrainer(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            trainerService.removeTrainer(id);
            redirectAttributes.addFlashAttribute("successMessage", "Trainer removed successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error removing trainer: " + e.getMessage());
        }
        return "redirect:/managerViewTrainers";
    }

    @PostMapping("/promoteTrainer/{id}")
    public String promoteTrainer(@PathVariable("id") int id) {
        Trainer trainer = trainerService.getTrainerById(id);
        if (trainer != null) {
            int currentRank = trainer.getRank();
            if (currentRank < 5) {
                trainer.updateRank(currentRank + 1);
                trainerService.saveTrainer(trainer);
            }
        }
        return "redirect:/managerViewTrainers";
    }

    @PostMapping("/demoteTrainer/{id}")
    public String demoteTrainer(@PathVariable("id") int id) {
        Trainer trainer = trainerService.getTrainerById(id);
        if (trainer != null) {
            int currentRank = trainer.getRank();
            if (currentRank > 3) {
                trainer.updateRank(currentRank - 1);
                trainerService.saveTrainer(trainer);
            }
        }
        return "redirect:/managerViewTrainers";
    }

    @GetMapping("/managerViewUsers")
    public String showUsers(Model model) {
        model.addAttribute("Users", userService.getAllUsers());
        return "managerViewUsers";
    }

    @GetMapping("/income")
    public String showIncomeReport(@RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate,
            Model model) {

        List<PaymentTransaction> paymentTransactions = paymentTransactionRepo.findByPurchasedDateBetween(startDate,
                endDate);
        for (PaymentTransaction transaction : paymentTransactions) {
            Income existingIncome = incomeRepo.findByIncomeId(transaction.getTransactionId());
            if (existingIncome == null) {
                Income income = new Income();
                income.setAmount(transaction.getPrice());
                income.setDate(transaction.getPurchasedDate());
                income.setDescription(transaction.getPlanType());
                income.setIncomeId(transaction.getTransactionId());
                incomeRepo.save(income);
            }
        }
        List<Income> incomeList = incomeRepo.findByDateBetween(startDate, endDate);
        double totalAmount = 0;
        for (Income income : incomeList) {
            totalAmount += income.getAmount();
        }
        model.addAttribute("incomeList", incomeList);
        model.addAttribute("totalAmount", totalAmount);
        return "income_report";
    }

    @GetMapping("/profit")
    public String showProfitReport(@RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate,
            Model model) {
        List<Income> incomeList = incomeRepo.findByDateBetween(startDate, endDate);
        List<Expense> expenseList = expenseRepo.findByDateBetween(startDate, endDate);
        double totalIncome = calculateTotalIncome(incomeList);
        double totalExpenses = calculateTotalExpenses(expenseList);
        double totalProfit = totalIncome - totalExpenses;

        model.addAttribute("incomeList", incomeList);
        model.addAttribute("expenseList", expenseList);
        model.addAttribute("totalProfit", totalProfit);

        return "profit_report";
    }

    private double calculateTotalIncome(List<Income> incomeList) {
        double totalIncome = 0;
        for (Income income : incomeList) {
            totalIncome += income.getAmount();
        }
        return totalIncome;
    }

    private double calculateTotalExpenses(List<Expense> expenseList) {
        double totalExpenses = 0;
        for (Expense expense : expenseList) {
            totalExpenses += expense.getAmount();
        }
        return totalExpenses;
    }

    @GetMapping("/expenses")
    public String showExpensesReport(@RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate,
            Model model) {
        List<Expense> expenseList = expenseRepo.findByDateBetween(startDate, endDate);

        double totalExpenses = 0;
        for (Expense expense : expenseList) {
            totalExpenses += expense.getAmount();
        }

        model.addAttribute("expenseList", expenseList);
        model.addAttribute("totalExpenses", totalExpenses);

        return "expenses_report";
    }

    @PostMapping("/removeUser/{email}")
    public String removeUser(@PathVariable("email") String email) {

        userService.removeUser(email);
        return "redirect:/managerViewUsers";
    }

    @PostMapping("sendNotification/{email}")
    public String sendNotification(@PathVariable("email") String email) {
        // Send notification to the user

        return "redirect:/managerViewUsers";
    }

    // without session for now
    @GetMapping("/manager_appointment")
    public String listAppoingments(Model model, @SessionAttribute("manager") Manager manager) {
        // Update the status of past appointments to "inactive"
        appointmentService.deactivatePastAppointments();

        List<Appointment> allAppointment = appointmentService.findAllAppointment();
        model.addAttribute("allAppointment", allAppointment);

        return "managerViewAppointment";
    }

    @GetMapping("/manager_view_plans")
    public String managerBrowsePlans(Model model) {
        List<Plan> plans = planService.findAllPlans();
        model.addAttribute("plans", plans);
        return "managerViewPlans";
    }

    @GetMapping("/editPlan/{id}")
    public String editPlanForm(@PathVariable("id") int id, Model model) {
        Plan plan = planService.findPlanById(id);
        if (plan == null) {
            return "redirect:/managerViewPlans";
        }
        model.addAttribute("plan", plan);
        return "editPlan";
    }

    @PostMapping("/updatePlan/{id}")
    public String updatePlan(@PathVariable("id") int id, @ModelAttribute Plan plan, Model model) {
        planService.savePlan(plan);
        return "redirect:/manager_view_plans";
    }

    @PostMapping("/send-email/{email}")
    public ResponseEntity<String> sendEmail(@PathVariable("email") String email) {
        try {
            User user = userService.getUserByEmail(email);
            emailService.sendMembershipReminderEmail(email, user.getName(), user.getActiveDate());
            return ResponseEntity.ok("{\"message\": \"Email sent successfully to " + email + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"Failed to send email due to an error\"}");
        }
    }

    @GetMapping("/manager_add_appointment")
    public String showAddAppointmentForm(@RequestParam(name = "classId", required = false) Integer classId,
            @RequestParam(name = "className", required = false) String className,
            @ModelAttribute User user, Model model,
            @ModelAttribute("trainer") Trainer trainer,
            @SessionAttribute("manager") Manager manager) {

        List<FitnessClass> classList = fitnessClassService.getAllClasses();
        List<User> userList = userService.getAllUsers();
        List<Trainer> trainerList = trainerService.getAllTrainers();

        // Formatting date and time for the frontend
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        String formattedDateTimeNow = now.format(formatter);
        LocalDateTime Activenow = LocalDateTime.now();
        DateTimeFormatter Activeformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        User existingUser = userService.getUserByEmail(user.getEmail());

        model.addAttribute("formattedDateTimeNow", Activenow.format(Activeformatter));
        model.addAttribute("classList", classList);
        model.addAttribute("formattedDateTimeNow", formattedDateTimeNow);
        model.addAttribute("manager", manager);
        model.addAttribute("user", existingUser);
        model.addAttribute("userList", userList);
        model.addAttribute("trainerList", trainerList);

        return "manager-add-appointment";
    }

    @PostMapping("/manager_add_appointment")
    public String addAppointment(@ModelAttribute Appointment appointment,
            @RequestParam("customerEmail") int userId,
            @RequestParam("classId") int classId,
            @RequestParam("trainerId") int trainerId,
            @RequestParam("datetime") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime datetime,
            Model model) {

        // Fetch the related FitnessClass and Trainer objects based on the IDs provided
        FitnessClass fitnessClass = fitnessClassService.getClassById(classId);
        Trainer trainer = trainerService.getTrainerById(trainerId);
        User user = userService.getUserById(userId);

        // Set the fetched entities to the appointment
        appointment.setFitnessClass(fitnessClass);
        appointment.setTrainer(trainer);

        // Set the user obtained from the form submission
        User retrivedUser = userService.getUserByEmail(user.getEmail());
        appointment.setUser(retrivedUser);
        appointment.setUser(user);

        // Set the date and time for the appointment
        appointment.setDate(datetime);

        // Set the status of the appointment to "active"
        appointment.setStatus("active");

        // Save the appointment to the database
        appointmentService.saveAppointment(appointment);

        return "redirect:/manager_home_page";
    }

    @GetMapping("/managerviewtrainerReview")
    public String viewReviews(Model model, @RequestParam("trainerId") int trainerId) {
        List<Trainer> trainers = trainerService.getAllTrainers();
        model.addAttribute("trainers", trainers);

        Trainer trainer = trainerService.getTrainerById(trainerId);
        List<Review> reviews = reviewService.findByTrainer(trainer);
        model.addAttribute("reviews", reviews);

        model.addAttribute("trainer", trainer);

        return "managerViewReviews";
    }

    @PostMapping("/removeReview/{id}")
    public String removeReviewById(@PathVariable("id") int id) {
        reviewService.deleteReview(id);
        return "redirect:/managerViewTrainers";
    }

    

}
