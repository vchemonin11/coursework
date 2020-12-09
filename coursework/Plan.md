# Курсовая работа. Пошаговая игра с реплеем
## Название игры
**Игра детерминант** (_определитель_)

## Правила игры
Игра на двух игроков.
В матрицу 3х3 по очереди без повторений расставляются числа от 1 до 9. (_Каждое число может быть использовано ровно один раз_)  
Когда все числа расставлены подсчитывается определитель матрицы.  
Выигрыш первого игрока равен проигрышу второго игрока и равен значению определителя. (_Если определитель отрицательный, то выигрывает второй игрок_)

## Базовая функциональность приложения
1) Возможность играть двум игрокам из консоли.  
2) Возможность откатиться на любой ход и продолжить оттуда.  
3) Возможность играть с AI.  

## Расширенная фунциональность
4) Сохранение всех игр и ходов в базу данных.  
5) Возможность запустить любую игру из базы и продолжить ее с любого места
6) Возможность играть через браузер. (ajax json api и клиент на html)  
7) Лидерборд лучших игроков (набравших больше всего очков за конкретную игру, и суммарно за все игры)