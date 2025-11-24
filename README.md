# 🔐 **AuthController.java** - Proceso de Autenticación

**ESTE ARCHIVO ES EL:** **#1. Proceso de Autenticación y Autorización**

---

## 🎯 **FUNCIONES PRINCIPALES QUE CONTIENE:**

### **1. 🏠 Página de Inicio**
```java
@GetMapping("/")
public String home() {
    return "redirect:/login";  // Redirige automáticamente al login
}
```

### **2. 🔐 Formulario de Login**
```java
@GetMapping("/login")
public String showLoginForm() {
    return "auth/login";  // Muestra la página de inicio de sesión
}
```

### **3. 📝 Registro de Nuevos Usuarios**
```java
@GetMapping("/register")
public String showRegistrationForm(Model model) {
    model.addAttribute("user", new UserRegistrationDTO());
    model.addAttribute("roles", Role.values());  // ADMIN, TEACHER, STUDENT
    return "auth/register";
}
```

### **4. ✅ Procesamiento de Registro**
```java
@PostMapping("/register")
public String registerUser(@Valid UserRegistrationDTO userDTO, BindingResult result) {
    // Validaciones:
    // - Campos obligatorios
    // - Email único
    // - Contraseña segura
    
    // Creación del usuario:
    User user = new User(firstName, lastName, email, password, role);
    userService.createUser(user);  // Guarda en base de datos
}
```

### **5. 🚫 Página de Acceso Denegado**
```java
@GetMapping("/access-denied")
public String accessDenied() {
    return "error/access-denied";  // Cuando un usuario no tiene permisos
}
```

---

## 📊 **Importante:**

**✅ Este controlador maneja:**
- **Registro de nuevos usuarios** en el sistema
- **Validación de datos** con Spring Validation
- **Control de duplicados** (email único)
- **Redirección inteligente** después del registro
- **Página de error** para acceso denegado

**🔒 Spring Security se encarga del:**
- Procesamiento real del login (en `/login` POST)
- Autenticación de credenciales
- Mantenimiento de sesiones
- Protección de rutas por roles




# 📚 **CourseController.java** - Proceso de Gestión de Cursos

**ESTE ARCHIVO ES EL:** **#2. Proceso de Gestión de Cursos**

---

## 🎯 **FUNCIONES PRINCIPALES QUE CONTIENE:**

### **1. 📋 Listar Cursos (Inteligente por Rol)**
```java
@GetMapping
public String listCourses(Model model) {
    User currentUser = getCurrentUser();
    List<Course> courses;
    
    switch (currentUser.getRole()) {
        case ADMIN:     // Ve TODOS los cursos
            courses = courseService.findAllActiveCourses();
            break;
        case TEACHER:   // Ve solo SUS cursos
            courses = courseService.findCoursesByTeacher(currentUser.getId());
            break;
        case STUDENT:   // Ve cursos donde ESTÁ INSCRITO
            courses = courseService.findCoursesByStudent(currentUser.getId());
            break;
        default:
            courses = List.of();
    }
    
    return "courses/list";
}
```

### **2. ➕ Crear Nuevos Cursos (Solo Profesores)**
```java
@GetMapping("/create")
public String showCreateForm(Model model) {
    // Solo profesores pueden crear cursos
    if (!currentUser.getRole().equals(Role.TEACHER)) {
        return "redirect:/access-denied";
    }
    return "courses/create";
}

@PostMapping("/create")
public String createCourse(@Valid Course course, BindingResult result) {
    // Crea el curso y asigna automáticamente al profesor actual
    courseService.createCourse(course, currentUser.getId());
}
```

### **3. 👀 Ver Detalles de Curso**
```java
@GetMapping("/{id}")
public String viewCourse(@PathVariable Long id, Model model) {
    Course course = courseService.findCourseById(id);
    
    // Estudiantes solo pueden ver cursos donde están inscritos
    if (currentUser.getRole().equals(Role.STUDENT) && 
        !courseService.isStudentEnrolled(id, currentUser.getId())) {
        return "redirect:/access-denied";
    }
    
    return "courses/view";
}
```

### **4. 🎯 Sistema de Inscripción**
```java
// Unirse con código
@PostMapping("/join")
public String joinCourse(@RequestParam String code) {
    // Estudiantes se unen usando código del curso
    Course course = courseService.findCourseByCode(code);
    courseService.enrollStudent(course.getId(), currentUser.getId());
}

// Inscripción directa
@GetMapping("/{id}/enroll")
public String enrollInCourse(@PathVariable Long id) {
    courseService.enrollStudent(id, currentUser.getId());
}
```

### **5. 👥 Gestión de Estudiantes (Solo Profesor)**
```java
@GetMapping("/{id}/students")
public String manageStudents(@PathVariable Long id, Model model) {
    // Solo el profesor dueño del curso puede gestionar estudiantes
    if (!course.getTeacher().getId().equals(currentUser.getId())) {
        return "redirect:/access-denied";
    }
    return "courses/students";
}

// Agregar estudiante manualmente
@PostMapping("/{id}/students/add")
public String addStudent(@PathVariable Long id, @RequestParam Long studentId) {
    courseService.enrollStudent(id, studentId);
}

// Remover estudiante
@PostMapping("/{id}/students/remove")
public String removeStudent(@PathVariable Long id, @RequestParam Long studentId) {
    courseService.removeStudent(id, studentId);
}
```

---

## 📊 **Importante:**

**✅ Este controlador maneja:**
- **Vistas diferentes** según el rol del usuario
- **Control de permisos** estricto (profesores solo ven sus cursos)
- **Sistema de códigos** para unirse a cursos
- **Gestión completa** de estudiantes inscritos
- **Validaciones** de acceso en cada operación

**🔐 Características de seguridad:**
- Estudiantes no pueden crear cursos
- Estudiantes solo ven cursos donde están inscritos
- Profesores solo gestionan SUS propios cursos
- Admin ve todo el sistema

**🎯 Es el "administrador de aulas" del sistema** que organiza los cursos y sus participantes.

**📈 Flujo típico:**
```
Profesor crea curso → Genera código → Estudiantes se unen → Profesor gestiona lista
```















# 📝 **TaskController.java** - Proceso Completo de Gestión de Tareas

**ESTE ARCHIVO ES EL:** **#3. Proceso Completo de Tareas**

---

## 🎯 **FUNCIONES PRINCIPALES QUE CONTIENE:**

### **1. 📋 Listado Inteligente de Tareas**
```java
@GetMapping
public String listTasks(Model model) {
    // VISTA DIFERENTE SEGÚN ROL:
    // 👨‍🏫 PROFESORES: Ve todas sus tareas creadas
    // 👨‍🎓 ESTUDIANTES: Ve solo tareas de sus cursos
    // 📊 Datos adicionales: estado de entregas, conteos
}
```

### **2. ➕ Creación de Nuevas Tareas**
```java
@GetMapping("/create")
public String showCreateForm(Long courseId, Model model) {
    // Solo profesores pueden crear tareas
    // Formulario con validación de fechas
    // Selección de curso automática
}

@PostMapping("/create")
public String createTask(@Valid Task task, Long courseId) {
    // Validación de datos
    // Asignación automática al curso
    // Notificación a estudiantes
}
```

### **3. 👀 Visualización Detallada de Tarea**
```java
@GetMapping("/{id}")
public String viewTask(@PathVariable Long id, Model model) {
    // 👨‍🎓 ESTUDIANTES: Ve su entrega + botón "Entregar"
    // 👨‍🏫 PROFESORES: Ve estadísticas + botón "Ver Entregas"
    // Validación de permisos por curso
}
```

### **4. ✏️ Edición de Tareas Existentes**
```java
@GetMapping("/{id}/edit")
public String showEditForm(@PathVariable Long id, Model model) {
    // Solo el profesor creador puede editar
    // Formulario pre-llenado con datos actuales
}

@PostMapping("/{id}/edit")
public String updateTask(@PathVariable Long id, @Valid Task task) {
    // Validación de cambios
    // Actualización en base de datos
}
```

### **5. 🗑️ Eliminación de Tareas**
```java
@PostMapping("/{id}/delete")
public String deleteTask(@PathVariable Long id) {
    // Solo profesores pueden eliminar
    // Confirmación implícita
    // Redirección con mensaje
}
```

---

## 🔄 **FLUJO COMPLETO QUE MANEJA:**

```
CREAR TAREA → ASIGNAR A CURSO → NOTIFICAR ESTUDIANTES 
    ↓
ESTUDIANTES VEN TAREA → ENTREGAN ARCHIVOS 
    ↓
PROFESOR CALIFICA → NOTIFICA RESULTADOS
    ↓
ESTUDIANTES VEN CALIFICACIONES
```

---

## 📊 **MAS CLARO:**

**✅ Este controlador maneja:**
- **Vistas diferentes** según el rol del usuario
- **Validación de permisos** por curso
- **Gestión completa del ciclo de vida** de tareas
- **Integración con sistema de entregas**
- **Estadísticas en tiempo real** para profesores

**🎯 Características técnicas:**
- **Mapeo inteligente** de datos para cada rol
- **Validaciones de seguridad** en cada operación
- **Mensajes de feedback** para el usuario
- **Integración con SubmissionController** para entregas

**🔗 Está conectado con:**
- **CourseController** - Para verificar inscripciones
- **SubmissionController** - Para gestionar entregas  
- **NotificationService** - Para notificaciones automáticas
















# 📤 **SubmissionController.java** - Proceso de Entregas y Calificaciones

**ESTE ARCHIVO ES EL:** **#3. Proceso Completo de Entregas de Tareas**

---

## 🎯 **FUNCIONES PRINCIPALES QUE CONTIENE:**

### **1. 📋 Listar Entregas por Tarea**
```java
@GetMapping("/task/{taskId}")
public String getSubmissionsByTask(@PathVariable Long taskId, Model model) {
    // Muestra TODAS las entregas de una tarea específica
    // Para que el PROFESOR pueda ver y calificar
}
```

### **2. 📝 Formulario de Entrega**
```java
@GetMapping("/submit/{taskId}")
public String showSubmissionForm(@PathVariable Long taskId, Model model) {
    // Formulario para que el ESTUDIANTE entregue su tarea
    // Puede incluir comentarios y archivos adjuntos
}
```

### **3. 🚀 Procesar Entrega**
```java
@PostMapping("/submit/{taskId}")
public String submitTask(@PathVariable Long taskId,
                       @RequestParam String comment,
                       @RequestParam MultipartFile file) {
    // Procesa la entrega del estudiante:
    // - Valida que esté inscrito en el curso
    // - Guarda el archivo en el servidor
    // - Crea el registro en base de datos
    // - NOTIFICA AL PROFESOR automáticamente
}
```

### **4. 🎓 Formulario de Calificación**
```java
@GetMapping("/{submissionId}/grade")
public String showGradeForm(@PathVariable Long submissionId, Model model) {
    // Formulario para que el PROFESOR califique
    // Muestra la entrega del estudiante
}
```

### **5. ✅ Procesar Calificación**
```java
@PostMapping("/{submissionId}/grade")
public String gradeSubmission(@PathVariable Long submissionId,
                            @RequestParam Integer grade,
                            @RequestParam String feedback) {
    // Procesa la calificación:
    // - Asigna puntuación
    // - Agrega feedback/comentarios
    // - Cambia estado a "GRADED"
    // - NOTIFICA AL ESTUDIANTE automáticamente
}
```

### **6. 📥 Descargar Archivos**
```java
@GetMapping("/{submissionId}/download")
public ResponseEntity<Resource> downloadFile(@PathVariable Long submissionId) {
    // Permite descargar el archivo entregado por el estudiante
    // Usa ResponseEntity para manejar archivos binarios
}
```

### **7. 👤 Mis Entregas**
```java
@GetMapping("/my-submissions")
public String getMySubmissions(Model model) {
    // Muestra al ESTUDIANTE todas sus entregas históricas
    // Con estados y calificaciones
}
```

---

## 📊 **Importante:**

**✅ Este controlador maneja el CICLO COMPLETO:**
```
ESTUDIANTE: Entrega tarea → PROFESOR: Califica → ESTUDIANTE: Recibe feedback
```

**🔔 SISTEMA DE NOTIFICACIONES AUTOMÁTICAS:**
- 📧 **Al entregar:** Notifica al profesor
- 📧 **Al calificar:** Notifica al estudiante

**📁 GESTIÓN DE ARCHIVOS:**
- Subida segura de archivos
- Descarga controlada
- Almacenamiento en sistema de archivos

**🎯 ROLES ESPECÍFICOS:**
- **ESTUDIANTES:** Pueden entregar y ver sus calificaciones
- **PROFESORES:** Pueden calificar y ver todas las entregas

**⚡ TECNOLOGÍAS USADAS:**
- `MultipartFile` para subida de archivos
- `ResponseEntity<Resource>` para descargas
- `RedirectAttributes` para mensajes flash
- Validación de permisos por rol

---



















# 💬 **MessageController.java** - Proceso de Mensajería

**ESTE ARCHIVO ES EL:** **#4. Proceso de Mensajería**

---

## 🎯 **FUNCIONES PRINCIPALES QUE CONTIENE:**

### **1. 📨 Bandeja de Entrada**
```java
@GetMapping
public String listMessages(Model model) {
    User currentUser = getCurrentUser();
    
    model.addAttribute("receivedMessages", messageService.getReceivedMessages(currentUser.getId()));
    model.addAttribute("sentMessages", messageService.getSentMessages(currentUser.getId()));
    model.addAttribute("unreadMessageCount", messageService.getUnreadMessageCount(currentUser.getId()));
    return "messages/list";
}
```

### **2. 📤 Mensajes Enviados**
```java
@GetMapping("/sent")
public String listSentMessages(Model model) {
    // Muestra solo los mensajes que el usuario ha enviado
    model.addAttribute("sentMessages", messageService.getSentMessages(currentUser.getId()));
    model.addAttribute("showSentOnly", true);
    return "messages/list";
}
```

### **3. ✍️ Componer Mensaje**
```java
@GetMapping("/compose")
public String showComposeForm(@RequestParam(required = false) Long to, Model model) {
    model.addAttribute("users", userService.findAllActiveUsers()); // Lista de destinatarios
    if (to != null) {
        model.addAttribute("recipientId", to); // Pre-seleccionar destinatario
    }
    return "messages/compose";
}
```

### **4. 🚀 Enviar Mensaje**
```java
@PostMapping("/send")
public String sendMessage(@RequestParam String subject, @RequestParam String content,
                        @RequestParam Long receiverId, RedirectAttributes redirectAttributes) {
    messageService.sendMessage(subject, content, currentUser.getId(), receiverId);
    redirectAttributes.addFlashAttribute("success", "Mensaje enviado exitosamente");
    return "redirect:/messages";
}
```

### **5. 👁️ Ver Mensaje Individual**
```java
@GetMapping("/{id}")
public String viewMessage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
    // Verificar permisos de seguridad
    if (!message.getSender().getId().equals(currentUser.getId()) && 
        !message.getReceiver().getId().equals(currentUser.getId())) {
        redirectAttributes.addFlashAttribute("error", "No tienes permiso para ver este mensaje");
        return "redirect:/messages";
    }
    
    // Marcar como leído automáticamente
    if (message.getReceiver().getId().equals(currentUser.getId()) && !message.getRead()) {
        messageService.markAsRead(id);
        message.setRead(true);
    }
    
    model.addAttribute("message", message);
    return "messages/view";
}
```

### **6. ✅ Marcar como Leído**
```java
@PostMapping("/{id}/read")
public String markAsRead(@PathVariable Long id) {
    messageService.markAsRead(id);
    return "redirect:/messages";
}
```

---

## 📊 **Importante:**

**✅ Este controlador maneja:**
- **Comunicación bidireccional** entre usuarios del sistema
- **Bandeja de entrada** con mensajes recibidos
- **Historial de mensajes enviados**
- **Sistema de notificaciones** (contador de no leídos)
- **Seguridad** (solo puedes ver tus mensajes)

**🔒 Características de Seguridad:**
- Verificación de permisos para cada mensaje
- No puedes ver mensajes de otros usuarios
- Marcado automático como leído al visualizar

**💡 Flujo Completo del Proceso:**
```
Componer → Validar → Enviar → Notificar → Marcar como leído
```

**🎯 Es el "sistema de correo interno"** que permite la comunicación entre profesores, estudiantes y administradores.





# 📊 **DashboardController.java** - Proceso de Dashboard Dinámico

**ESTE ARCHIVO ES EL:** **#5. Proceso de Dashboard Dinámico por Rol**

---

## 🎯 **FUNCIONES PRINCIPALES QUE CONTIENE:**

### **1. 🔄 Obtención del Usuario Actual**
```java
private User getCurrentUser() {
    // OBTIENE EL USUARIO AUTENTICADO ACTUALMENTE
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();  // Email del usuario logueado
    return userService.findUserByEmail(email)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
}
```

### **2. 🎪 Dashboard Principal - Punto de Entrada Único**
```java
@GetMapping("/dashboard")
public String showDashboard(Model model) {
    // 1. OBTENER USUARIO ACTUAL
    User currentUser = getCurrentUser();
    
    // 2. DATOS COMUNES PARA TODOS LOS ROLES
    model.addAttribute("currentUser", currentUser);
    model.addAttribute("unreadMessageCount", messageService.getUnreadMessageCount(currentUser.getId()));
    model.addAttribute("unreadNotificationCount", notificationService.getUnreadNotificationCount(currentUser.getId()));
    
    // 3. REDIRIGIR AL DASHBOARD ESPECÍFICO SEGÚN ROL
    switch (currentUser.getRole()) {
        case ADMIN: return showAdminDashboard(model, currentUser);      // 👨‍💼 Admin
        case TEACHER: return showTeacherDashboard(model, currentUser);  // 👩‍🏫 Profesor  
        case STUDENT: return showStudentDashboard(model, currentUser);  // 👨‍🎓 Estudiante
        default: return "redirect:/access-denied";                      // 🚫 Acceso denegado
    }
}
```

### **3. 👨‍💼 Dashboard para ADMINISTRADOR**
```java
private String showAdminDashboard(Model model, User user) {
    // ESTADÍSTICAS GLOBALES DEL SISTEMA
    model.addAttribute("totalUsers", userService.findAllActiveUsers().size());        // Total usuarios
    model.addAttribute("totalCourses", courseService.findAllActiveCourses().size());  // Total cursos
    model.addAttribute("totalTasks", taskService.findTasksByTeacher(user.getId()).size()); // Total tareas
    model.addAttribute("studentCount", userService.findUsersByRole(Role.STUDENT).size());  // Total estudiantes
    model.addAttribute("teacherCount", userService.findUsersByRole(Role.TEACHER).size());  // Total profesores
    model.addAttribute("pendingTasks", 0);      // Tareas pendientes (por implementar)
    model.addAttribute("todayMessages", 0);     // Mensajes hoy (por implementar)
    
    return "dashboard/admin-dashboard";  // 📊 Vista específica para admin
}
```

### **4. 👩‍🏫 Dashboard para PROFESOR**
```java
private String showTeacherDashboard(Model model, User user) {
    // DATOS ESPECÍFICOS PARA PROFESORES
    model.addAttribute("courses", courseService.findCoursesByTeacher(user.getId()));     // Cursos que imparte
    model.addAttribute("tasks", taskService.findTasksByTeacher(user.getId()));          // Tareas creadas
    model.addAttribute("pendingSubmissions", submissionService.findSubmissionsByTeacher(user.getId())
        .stream().filter(s -> s.getGrade() == null).count());  // 📦 Entregas pendientes de calificar
    
    return "dashboard/teacher-dashboard";  // 🎯 Vista específica para profesor
}
```

### **5. 👨‍🎓 Dashboard para ESTUDIANTE**
```java
private String showStudentDashboard(Model model, User user) {
    // DATOS ESPECÍFICOS PARA ESTUDIANTES
    model.addAttribute("courses", courseService.findCoursesByStudent(user.getId()));     // Cursos inscritos
    model.addAttribute("upcomingTasks", taskService.findUpcomingTasksByStudent(user.getId()));  // ⏰ Tareas próximas
    
    return "dashboard/student-dashboard";  // 📚 Vista específica para estudiante
}
```

---

## 📊 **Importante:**

**✅ Este controlador maneja:**
- **Un único endpoint (/dashboard)** que muestra vistas diferentes
- **Detección automática del rol** del usuario logueado
- **Datos personalizados** para cada tipo de usuario
- **Experiencia de usuario adaptativa** sin cambiar URLs

**🎯 Características únicas:**
- **Mismo URL, diferente contenido** según el rol
- **Métricas relevantes** para cada perfil
- **Contadores en tiempo real** (mensajes, notificaciones)
- **Redirección inteligente** sin intervención del usuario

**🚀 Es el "centro de control" personalizado** de cada usuario en el sistema.**#   S i s t e m a - d e - A s i g n a r - T a r e a s - y - e n v - o - d e - m e n s a j e s  
 