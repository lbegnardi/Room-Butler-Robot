/*
===============================================================
resourceModel.pl
===============================================================
*/

model( environment, roommap, state(unknown) ).		%% the actual map will be obtained at the start
model( actuator, robot,      state(stopped)	 ).		%% initial state
model( sensor,   sonarRobot, state(unknown)	 ).		%% initial state
model( resource, table,		 state([ ])	 ).		%% initial state
model( resource, pantry,	 state([ dish, dish, dish, dish, dish, dish, dish, dish, dish, dish, dish, dish, dish, dish, dish ]) ).		%% initial state
model( resource, dishwasher, state([ ])	 ).		%% initial state

%% environment actions
action(roommap, update(V)) :- changeModel( environment, roommap, V ).

%% movement actions
action(robot, move(w)) :- changeModel( actuator, robot, movingForward  ).
action(robot, move(s)) :- changeModel( actuator, robot, movingBackward ).
action(robot, move(a)) :- changeModel( actuator, robot, rotateLeft     ).
action(robot, move(d)) :- changeModel( actuator, robot, rotateRight    ).
action(robot, move(h)) :- changeModel( actuator, robot, stopped        ).
action(robot, move(l)) :- changeModel( actuator, robot, rotateLeft90   ).
action(robot, move(r)) :- changeModel( actuator, robot, rotateRight90  ).

%%application actions
action(ROOMRES, remove(dish)) :- changeModel( resource, ROOMRES, removeDish ), !.
action(ROOMRES, add(dish)) :- changeModel( resource, ROOMRES, addDish ), !.
action(ROOMRES, remove(F)) :- changeModel(	resource, ROOMRES, removeFood(F)).
action(ROOMRES, add(F))	:- changeModel(	resource, ROOMRES, addFood(F)).

%%sensor actions
action(sonarRobot, V)  :- changeModel( sensor, sonarRobot, V  ).

changeModel( CATEG, NAME, addDish ) :-
	replaceRule( model(CATEG, NAME, state(X)), model( CATEG, NAME, state([dish|X])) ), !.

changeModel( CATEG, NAME, removeDish ) :-
	replaceRule( model(CATEG, NAME, state([dish|X])), model( CATEG, NAME, state(X)) ), !.

changeModel( CATEG, NAME, addFood(VALUE) ) :-
	replaceRule( model(CATEG, NAME, state(X)), model( CATEG, NAME, state([VALUE|X])) ), !.

changeModel( CATEG, NAME, removeFood(VALUE) ) :-
	model( CATEG, NAME, state(X)),
	delete_one(VALUE, X, X1),
	replaceRule( model(CATEG, NAME, state(X)), model( CATEG, NAME, state(X1)) ), !.
	
changeModel( CATEG, NAME, VALUE ) :-
	replaceRule( model(CATEG,NAME,_),  model(CATEG,NAME,state(VALUE)) ).
	%% showResourceModel.	%% at each change, show the model

showResourceModel :- 
	output("RESOURCE MODEL ---------- "),
	showResources,
	output("--------------------------").
		
showResources :- 
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

initResourceTheory :- output("resourceModel loaded").
:- initialization(initResourceTheory).