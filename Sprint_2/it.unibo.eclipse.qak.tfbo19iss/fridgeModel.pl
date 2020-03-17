/*
===============================================================
fridgeModel.pl
===============================================================
*/
 
model( resource, fridge, state([ fruit, beef ]) ).	%% initial state

%%application actions
action(ROOMRES, remove(F)) :- changeModel(	resource, ROOMRES, removeFood(F)).
action(ROOMRES, add(F))	:- changeModel(	resource, ROOMRES, addFood(F)).

changeModel( CATEG, NAME, addFood(VALUE) ) :-
	replaceRule( model(CATEG, NAME, state(X)), model( CATEG, NAME, state([VALUE|X])) ), !.

changeModel( CATEG, NAME, removeFood(VALUE) ) :-
	model( CATEG, NAME, state(X)),
	delete_one(VALUE, X, X1),
	replaceRule( model(CATEG, NAME, state(X)), model( CATEG, NAME, state(X1)) ), !.
	
changeModel( CATEG, NAME, VALUE ) :-
	replaceRule( model(CATEG,NAME,_),  model(CATEG,NAME,state(VALUE)) ).

showFridgeModel :- 
	output("FRIDGE MODEL ---------- "),
	showFridge,
	output("--------------------------").
		
showFridge :- 
 	model( CATEG, NAME, STATE ),
 	output( model( CATEG, NAME, STATE ) ),
	fail.
showResources.			

delete_one(_, [], []).
delete_one(X, [X|T], T).
delete_one(X, [H|T], [H|R]) :-
	delete_one(X, T, R).

contains(X, [X|T]).
contains(X, [_|T]) :-
	contains(X, T).

output( M ) :- stdout <- println( M ).

initResourceTheory :- output("fridgeModel loaded").
:- initialization(initResourceTheory).