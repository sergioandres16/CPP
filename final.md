## ¿Qué ventajas e inconvenientes encontraste al usar multithreading/multiprocessing para este problema?

Al trabajar con multithreading, la principal ventaja fue la facilidad para compartir variables entre hilos y el bajo consumo de recursos. Sin embargo, el GIL de Python limitó la ejecución paralela real.

Con multiprocessing, aunque obtuve una verdadera ejecución paralela y mejor seguridad por la memoria separada, resultó ser más complejo de implementar y requirió más recursos del sistema.

## ¿En qué casos recomendarías el uso de multithreading sobre multiprocessing y viceversa?

Multithreading es ideal para aplicaciones que realizan principalmente operaciones de entrada/salida o requieren compartir datos frecuentemente. Por ejemplo: programas interactivos, juegos simples o aplicaciones que manejan inputs del usuario.

Multiprocessing es mejor para programas que necesitan realizar cálculos intensivos o aprovechar múltiples núcleos del procesador. Por ejemplo: procesamiento de imágenes, análisis de datos grandes o simulaciones complejas.

## ¿Cuál es el mejor para este problema?

Para este juego de adivinanza, multithreading es definitivamente la mejor opción. El programa principalmente maneja entrada de usuario y actualiza estados compartidos, no realiza cálculos complejos.

La implementación con hilos resultó más simple, eficiente y adecuada para las necesidades del juego. Usar multiprocessing sería como usar un martillo para poner una tachuela - demasiada herramienta para una tarea simple.
