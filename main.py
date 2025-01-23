# Online Python compiler (interpreter) to run Python online.
# Write Python 3 code in this online editor and run it.
# Get started with interactive Python!
# Supports Python Modules: builtins, math,pandas, scipy 
# matplotlib.pyplot, numpy, operator, processing, pygal, random, 
# re, string, time, turtle, urllib.request
A = int(input("Введите число A (A > B): "))
B = int(input("Введите число B: "))

# Проверяем условие A > B
if A <= B:
    print("Ошибка: A должно быть больше B.")
else:
    # Создаем список для хранения нечетных чисел
    odd_numbers = []

    # Проходим по всем числам от A до B включительно
    for number in range(A, B - 1, -1):
        if number % 2 != 0:  # Проверяем, является ли число нечетным
            odd_numbers.append(number)

    # Выводим все найденные нечетные числа
    print("Нечетные числа от", A, "до", B, "в порядке убывания:")
    for odd in odd_numbers:
        print(odd)

