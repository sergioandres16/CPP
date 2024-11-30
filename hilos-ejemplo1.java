import random
import time
from threading import Thread, Lock, Event

# Lock evita que varios hilos modifiquen las variables al mismo tiempo
bloqueo = Lock()

# Event permite detener todos los hilos cuando el juego termina
evento_parar = Event()

# Variables globales del juego
frutas = ["manzana", "platano", "naranja", "fresa", "mango", 
          "pera", "uva", "melon", "piña", "kiwi"]
palabra = ""
letras_adivinadas = set()
letras_incorrectas = set()
intentos_restantes = 8
tiempo_restante = 90
juego_ganado = False
juego_perdido = False

def reiniciar_juego():
    # Reinicia todas las variables del juego
    global palabra, letras_adivinadas, letras_incorrectas, intentos_restantes, tiempo_restante, juego_ganado, juego_perdido
    palabra = random.choice(frutas).lower()
    letras_adivinadas = set()
    letras_incorrectas = set()
    intentos_restantes = 8
    tiempo_restante = 90
    juego_ganado = False
    juego_perdido = False
    print("\nNueva palabra seleccionada:")
    print(f"Palabra a adivinar: {mostrar_palabra()}")

def revisar_letra(letra):
    # Revisa si la letra está en la palabra
    global intentos_restantes, juego_ganado, juego_perdido
    # Usamos bloqueo para evitar que otros hilos modifiquen las variables mientras las usamos
    with bloqueo:
        if juego_perdido or juego_ganado:
            return False
            
        letra = letra.lower()
        if letra in letras_adivinadas or letra in letras_incorrectas:
            return False
        
        if letra in palabra:
            letras_adivinadas.add(letra)
            print("\n¡Letra correcta!")
            print(f"Palabra actual: {mostrar_palabra()}")
        else:
            letras_incorrectas.add(letra)
            intentos_restantes -= 1
            if intentos_restantes > 0:
                print(f"\n¡Letra incorrecta! Te quedan {intentos_restantes} intentos")
                print(f"Palabra actual: {mostrar_palabra()}")
            else:
                juego_perdido = True
                print(f"\n¡PERDISTE! Te quedaste sin intentos. La palabra era: {palabra}")
        
        juego_ganado = all(letra in letras_adivinadas for letra in palabra)
        if juego_ganado:
            print(f"\n¡GANASTE! Has adivinado la palabra: {palabra}")
        
        return True

def mostrar_palabra():
    # Muestra la palabra con guiones bajos para letras no adivinadas
    return ' '.join(letra if letra in letras_adivinadas else '_' for letra in palabra)

def hilo_juego():
    # Hilo que maneja la lógica principal del juego
    global tiempo_restante, juego_perdido
    while not evento_parar.is_set():
        with bloqueo:
            if tiempo_restante <= 0:
                juego_perdido = True
                print(f"\n¡PERDISTE! Se acabó el tiempo. La palabra era: {palabra}")
                reiniciar_juego()
            elif juego_perdido or juego_ganado:
                reiniciar_juego()
        time.sleep(0.1)

def hilo_temporizador():
    # Hilo que maneja el tiempo
    global tiempo_restante
    while not evento_parar.is_set():
        time.sleep(1)
        with bloqueo:
            if tiempo_restante > 0 and not juego_perdido and not juego_ganado:
                tiempo_restante -= 1

def hilo_estado():
    # Hilo que muestra actualizaciones cada 10 segundos
    while not evento_parar.is_set():
        time.sleep(10)
        with bloqueo:
            if not juego_perdido and not juego_ganado:
                print("\n")
                print("#"*50)
                print("# Estado del juego:")
                print(f"#   Letras correctas: {', '.join(sorted(letras_adivinadas)) if letras_adivinadas else 'Ninguna'}")
                print(f"#   Letras incorrectas: {', '.join(sorted(letras_incorrectas)) if letras_incorrectas else 'Ninguna'}")
                print(f"#   Intentos restantes: {intentos_restantes}")
                print(f"#   Tiempo restante: {tiempo_restante} segundos")
                print("#"*50)

def main():
    print("¡Bienvenido al juego de adivinanza de frutas!")
    print("Tienes 90 segundos y 8 intentos para adivinar la palabra.")
    
    # Inicializar el juego
    reiniciar_juego()
    
    # Crear y configurar los hilos
    hilo_principal = Thread(target=hilo_juego)
    hilo_tiempo = Thread(target=hilo_temporizador)
    hilo_actualizacion = Thread(target=hilo_estado)
    
    hilos = [hilo_principal, hilo_tiempo, hilo_actualizacion]
    for hilo in hilos:
        #El programa no terminaría hasta que todos los hilos terminen
        hilo.daemon = True
        hilo.start()
    
    try:
        while True:
            letra = input("\nIngresa una letra: ")
            if len(letra) != 1:
                print("Por favor ingresa solo una letra.")
                continue
                
            if not letra.isalpha():
                print("Por favor ingresa una letra válida.")
                continue
                
            if revisar_letra(letra):
                pass
            else:
                if not juego_perdido and not juego_ganado:
                    print("Ya has intentado esa letra.")
    
    except KeyboardInterrupt:
        print("\nJuego terminado por el usuario.")
        evento_parar.set()
        for hilo in hilos:
            hilo.join()

if __name__ == "__main__":
    main()
