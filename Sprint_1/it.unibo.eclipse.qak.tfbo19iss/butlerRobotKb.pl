preparation([dish, fruit]).

goal(pantry, 0, 4).
goal(fridge, 5, 0).
goal(dishwasher, 5, 4).
goal(table, 5, 3).
goal(home, 0, 0).

showPreparationSet :-
	preparation(X),
	output(preparation(X)),
	fail.
showPreparationSet.

output( M ) :- stdout <- println( M ).

initPreparationTheory :- output("preparationSet loaded").
:- initialization(initPreparationTheory).