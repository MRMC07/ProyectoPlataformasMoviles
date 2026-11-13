package com.example.proyectoplataformasmoviles.domain.model

data class Listing(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val category: String,
    val sellerName: String,
    val imageUrl: String,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val faculty: String = "Comunidad universitaria",
    val deliveryMode: String = "A convenir",
    val turnaroundTime: String = "Respuesta rápida (< 2 hrs)",
    val correoVendedor: String = "",
    val propietario: String = "",
    val universidad: String = "",
    val includes: List<String> = emptyList()
)

val defaultUvgListings: List<Listing> = listOf(
    Listing(
        id = "uvg_tutoria_calculo",
        title = "Tutoría de Cálculo II, Álgebra Lineal y Física",
        description = "Sesiones prácticas e intensivas de resolución de problemas, preparación para exámenes parciales y resolución de dudas conceptuales con metodología paso a paso.",
        price = 75.0,
        category = "Tutorías",
        sellerName = "Carlos Mendoza",
        imageUrl = "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=800&auto=format&fit=crop&q=80",
        rating = 4.9,
        reviewCount = 32,
        faculty = "Ingeniería Mecatrónica UVG (4to año)",
        deliveryMode = "Presencial CIT UVG o Google Meet",
        turnaroundTime = "Respuesta en < 30 min",
        includes = listOf(
            "1 hora de resolución guiada de ejercicios",
            "PDF con formulario y notas de la sesión",
            "Resolución de dudas posteriores por chat"
        )
    ),
    Listing(
        id = "uvg_dev_software",
        title = "Desarrollo de Apps Móviles y Sitios Web para Proyectos",
        description = "Construcción de aplicaciones modernas en Jetpack Compose, Flutter o React. Ideal para proyectos de fin de curso, MVPs de emprendimientos o tesis.",
        price = 450.0,
        category = "Tecnología",
        sellerName = "Sofía Castillo",
        imageUrl = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800&auto=format&fit=crop&q=80",
        rating = 5.0,
        reviewCount = 21,
        faculty = "Ciencias de la Computación UVG (5to año)",
        deliveryMode = "Virtual / Laboratorios CIT",
        turnaroundTime = "Entrega en 3 a 5 días",
        includes = listOf(
            "Interfaz responsiva y moderna",
            "Código limpio documentado en GitHub",
            "Sesión de traspaso y asesoría técnica"
        )
    ),
    Listing(
        id = "uvg_diseno_branding",
        title = "Diseño de Marca, Identidad Visual y Pitch Decks",
        description = "Diseño de logotipos, manuales de marca, paletas tipográficas y presentaciones de alto impacto para validar emprendimientos y concursos universitarios.",
        price = 150.0,
        category = "Diseño",
        sellerName = "Alejandro Morales",
        imageUrl = "https://images.unsplash.com/photo-1561070791-2526d30994b5?w=800&auto=format&fit=crop&q=80",
        rating = 4.8,
        reviewCount = 27,
        faculty = "Diseño y Comunicación UVG",
        deliveryMode = "Virtual y revisiones en Plaza Central",
        turnaroundTime = "Bocetos en 48 hrs",
        includes = listOf(
            "Logotipo en vector (SVG, PNG de alta resolución)",
            "Guía de tipografías y colores",
            "Plantilla editable para diapositivas"
        )
    ),
    Listing(
        id = "uvg_prototipado_cit",
        title = "Impresión 3D y Prototipado Rápido Maker CIT",
        description = "Servicio de impresión 3D en filamentos PLA, PETG y resina de alta precisión para prototipos mecánicos, maquetas arquitectónicas y carcasas de electrónica.",
        price = 60.0,
        category = "Tecnología",
        sellerName = "Andrea Gómez",
        imageUrl = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=800&auto=format&fit=crop&q=80",
        rating = 4.9,
        reviewCount = 38,
        faculty = "Maker Space CIT / Ing. Mecánica",
        deliveryMode = "Entrega en Edificio CIT UVG",
        turnaroundTime = "Entrega en 24 a 48 hrs",
        includes = listOf(
            "Revisión y laminación de archivos STL/OBJ",
            "Acabado limpio y soporte técnico",
            "Consejos de tolerancias y ensamble"
        )
    ),
    Listing(
        id = "uvg_cafe_artesanal",
        title = "Café de Especialidad y Snacks Artesanales de Estudio",
        description = "Café de origen cosechado en fincas de Guatemala (Antigua y Huehuetenango), molido fresco o preparado, acompañado de brownies y galletas de avena.",
        price = 35.0,
        category = "Comida",
        sellerName = "Diego Estrada",
        imageUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=800&auto=format&fit=crop&q=80",
        rating = 5.0,
        reviewCount = 49,
        faculty = "Bioquímica y Gestión de Negocios UVG",
        deliveryMode = "Entrega inmediata en Cafetería UVG",
        turnaroundTime = "Entrega inmediata en recesos",
        includes = listOf(
            "Empaque biodegradable y amigable con el ambiente",
            "Tueste reciente de alta calidad",
            "Opción sin azúcar ni gluten"
        )
    ),
    Listing(
        id = "uvg_bioestadistica",
        title = "Asesoría en Bioestadística, R y Análisis de Datos",
        description = "Orientación experta en diseño experimental, contrastes de hipótesis, modelos lineales y visualización de datos científicos en R y Python para proyectos y tesis.",
        price = 90.0,
        category = "Consultoría",
        sellerName = "Valeria Fuentes",
        imageUrl = "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=800&auto=format&fit=crop&q=80",
        rating = 4.9,
        reviewCount = 19,
        faculty = "Biología y Ciencias de Datos UVG",
        deliveryMode = "Biblioteca UVG / Google Meet",
        turnaroundTime = "Respuesta el mismo día",
        includes = listOf(
            "Revisión de base de datos y supuestos",
            "Código de R comentado paso a paso",
            "Interpretación gráfica y estadística para reporte"
        )
    )
)


