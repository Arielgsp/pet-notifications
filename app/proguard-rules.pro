# Reglas para la versión final (R8).

# Los enums se guardan por nombre (ajustes y base de datos: mascota, acción al tocar, repetición,
# origen del aviso). Si R8 los renombrara o los convirtiera en números, se perderían los datos.
-keepclassmembers enum com.petnotifications.** {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
