import random
import time
from multiprocessing import Process, Value, Array, Manager, Lock
import os

def inicializar_palabra():
    frutas = ["manzana", "platano", "naranja", "fresa", "mango", 
              "pera", "uva", "melon", "piña", "kiwi"]
    return random.choice(frutas).lower()

def mostrar_progreso(palabra, letras_adivinadas):
    return ' '.join(letra if letra in letras_adivinadas else '_' for letra in palabra)

def proceso_juego(palabra, letras_adivinadas, letras_incorrectas, intentos, tiempo, juego_terminado, lock):
    while not juego_terminado.value:
        with lock:
            if tiempo.value <= 0:
                print(f"\n¡PERDISTE! Se acabó el tiempo. La palabra era: {palabra}")
                juego_terminado.value = 1
            elif intentos.value <= 0:
                print(f"\n¡PERDISTE! Te quedaste sin intentos. La palabra era: {palabra}")
                juego_terminado.value = 1
            # Verificar victoria
            if all(letra in letras_adivinadas for letra in palabra):
                print(f"\n¡GANASTE! Has adivinado la palabra: {palabra}")
                juego_terminado.value = 1
        time.sleep(0.1)

def proceso_temporizador(tiempo, juego_terminado, lock):
    while not juego_terminado.value and tiempo.value > 0:
        time.sleep(1)
        with lock:
            tiempo.value -= 1

def proceso_estado(tiempo, intentos, letras_adivinadas, letras_incorrectas, juego_terminado, lock):
    while not juego_terminado.value:
        time.sleep(10)
        with lock:
            if not juego_terminado.value:
                print("\nEstado del juego:")
                print(f"Letras correctas: {', '.join(sorted(letras_adivinadas)) if letras_adivinadas else 'Ninguna'}")
                print(f"Letras incorrectas: {', '.join(sorted(letras_incorrectas)) if letras_incorrectas else 'Ninguna'}")
                print(f"Intentos restantes: {intentos.value}")
                print(f"Tiempo restante: {tiempo.value} segundos")

if __name__ == "__main__":
    # Inicializar variables compartidas
    manager = Manager()
    lock = Lock()
    
    tiempo = Value('i', 90)
    intentos = Value('i', 8)
    juego_terminado = Value('i', 0)
    letras_adivinadas = manager.list()
    letras_incorrectas = manager.list()
    
    # Seleccionar palabra aleatoria
    palabra = inicializar_palabra()
    
    # Crear procesos
    p_juego = Process(target=proceso_juego, args=(palabra, letras_adivinadas, letras_incorrectas, intentos, tiempo, juego_terminado, lock))
    p_temporizador = Process(target=proceso_temporizador, args=(tiempo, juego_terminado, lock))
    p_estado = Process(target=proceso_estado, args=(tiempo, intentos, letras_adivinadas, letras_incorrectas, juego_terminado, lock))
    
    # Iniciar procesos
    procesos = [p_juego, p_temporizador, p_estado]
    for p in procesos:
        p.daemon = True
        p.start()
    
    # Mensaje de bienvenida
    print("¡Bienvenido al juego de adivinanza de frutas!")
    print("Tienes 90 segundos y 8 intentos para adivinar la palabra.")
    print(f"Palabra a adivinar: {mostrar_progreso(palabra, letras_adivinadas)}")
    
    # Ciclo principal del juego
    try:
        while not juego_terminado.value:
            letra = input("\nIngresa una letra: ").lower()
            
            if len(letra) != 1:
                print("Por favor ingresa solo una letra.")
                continue
                
            if not letra.isalpha():
                print("Por favor ingresa una letra válida.")
                continue
                
            with lock:
                if letra in letras_adivinadas or letra in letras_incorrectas:
                    print("Ya has intentado esa letra.")
                    continue
                
                if letra in palabra:
                    letras_adivinadas.append(letra)
                    print("¡Letra correcta!")
                else:
                    letras_incorrectas.append(letra)
                    intentos.value -= 1
                    print(f"¡Letra incorrecta! Te quedan {intentos.value} intentos")
                
                print(f"Palabra actual: {mostrar_progreso(palabra, letras_adivinadas)}")
    
    except KeyboardInterrupt:
        print("\nJuego terminado por el usuario.")
        juego_terminado.value = 1
    
    finally:
        # Terminar procesos
        for p in procesos:
            p.terminate()
            p.join()
