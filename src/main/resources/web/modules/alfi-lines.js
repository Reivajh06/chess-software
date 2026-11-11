export const alfiLines = {
	replayStart: {
		calm: ["a rerun. let's see if it ends differently this time.", "back to the scene of the crime. i'll narrate."],
		funny: ["ah, the archives. grab a snack. this one's a classic. probably."],
	},
	replayCapture: {
		calm: ["there goes a {piece}. it had such a promising career.", "one {piece} fewer. the board gets roomier."],
		funny: ["{piece} taken. somebody is going to feel that later."],
	},
	replayQueen: {
		calm: ["the queen leaves the board. bold. or careless. same thing, really.", "queen gone. the board just got a lot quieter."],
		funny: ["losing a queen. that's the expensive kind of mistake."],
	},
	replayCastle: {
		calm: ["the king hides behind a wall. very relatable.", "the king packs up and moves next to the rook. cozy."],
		funny: ["two pieces move at once. the king calls it strategy."],
	},
	replayPromotion: {
		calm: ["a pawn made it all the way. now it's a {piece}. hard work pays.", "pawn becomes a {piece}. careers really can change late."],
		funny: ["promotion. the little pawn finally got the corner office."],
	},
	replayWin: {
		calm: ["and that's the win. you can stop rewinding it now.", "the end. you won. i checked twice. still true."],
		funny: ["victory again. it doesn't get less smug the second time."],
	},
	replayLoss: {
		calm: ["and there it is. the part we don't talk about.", "the end. still a loss. replays don't take requests."],
		funny: ["same ending as last time. weird how that works."],
	},
	replayDraw: {
		calm: ["a draw. everyone shakes hands and pretends they're fine.", "nobody won. very polite. very boring."],
		funny: ["draw. the board refuses to pick a favorite."],
	},
	replayEnd: {
		calm: ["and that's the game. roll credits.", "that's every move. i ran out of things to judge."],
		funny: ["the end. no refunds."],
	},
	bossMute: {
		calm: [
			"mute button? sure. see if it can save your king.",
			"you can click it. i can keep talking. lovely arrangement.",
			"same button, same answer. almost like there's a pattern.",
		],
		funny: [
			"great button. shame it has no authority here.",
			"shh... oh, were you talking to me? unfortunate.",
		],
	},
	bossHide: {
		calm: [
			"hide me? ambitious. try hiding your king first.",
			"close the panel. i'll still have a move waiting.",
		],
		funny: [
			"oh, an escape plan. with a little button. adorable.",
		],
	},
	bossTheme: {
		calm: [
			"the board stays. you'll want something familiar.",
			"i chose these colors. consider it your first warning.",
		],
		funny: [
			"redecorating? sure. that was the problem with your position.",
		],
	},
	bossVolume: {
		calm: [
			"that's quiet enough. some warnings deserve an audience.",
			"lower the volume. the next move still lands.",
		],
		funny: [
			"trying to soften the soundtrack? the board didn't get the memo.",
		],
	},
	bossPoke: {
		calm: [
			"it's your turn. unless this is the whole strategy.",
			"the pieces are down there. i'm the other problem.",
			"keep clicking. your king could use the distraction.",
			"still here. remarkably resistant to fingers.",
		],
		funny: [
			"looking for a way out? try the board. fewer buttons.",
			"every poke gives me time to plan. very considerate.",
			"all those clicks. and somehow your king hasn't moved.",
		],
	},
	bossPoked: {
		calm: [
			"careful. that's paint you're wearing down. my patience is fine.",
			"five pokes. lovely warm-up. now move something that matters.",
		],
		funny: [
			"one more poke won't change the position. but do test the theory.",
		],
	},
	bossChecked: {
		calm: ["check. my king's under attack. so you do have a plan.", "you found my king. let's see if you can keep the appointment."],
		funny: ["my king's under attack. i'll cancel its nap. tragic."],
	},
	bossCheck: {
		calm: ["check. your king's under attack. i'd answer that.", "your king needs a safe move. take a moment. just the one."],
		funny: ["your king's under attack. royal service has really gone downhill."],
	},
	bossBlunder: {
		calm: ["a free piece. you remembered my birthday. how odd.", "that move left a piece for me. i'll spare you the thank-you note."],
		funny: ["well. that made my next move terribly difficult to resist."],
	},
	bossQueenLost: {
		calm: ["my queen's gone. good. now i have to pay attention.", "you took my queen. enjoy the souvenir. we're still playing."],
		funny: ["queen taken. i was rather attached. unlike her, apparently."],
	},
	bossQueenWon: {
		calm: ["your queen's off the board. hear how quiet it got?", "i'll take your queen. you can keep the long pause."],
		funny: ["queen removed. such a spacious board you've given me."],
	},
	bossLostPiece: {
		calm: ["one of my pieces gone. try not to frame it just yet.", "you took that piece. i suppose i should move the others."],
		funny: ["nice capture. was that the plan, or the opening act?"],
	},
	bossAhead: {
		calm: ["i've got the advantage. you can stop pretending it's part of your plan.", "i'm ahead. funny how the board keeps agreeing with me."],
		funny: ["i could ease up. sounds exhausting, though. changing habits."],
	},
	bossBehind: {
		calm: ["you're ahead. i'd congratulate you, but we're still playing.", "enjoy the advantage. it doesn't come with a guarantee."],
		funny: ["oh, i'm losing? how rude of the numbers to mention it."],
	},
	bossComeback: {
		calm: ["the lead changed hands. hope you weren't getting attached."],
		funny: ["funny thing about a bishop. always an angle."],
	},
	bossPromotion: {
		calm: ["your pawn reached the far edge and became a new piece. very ambitious.", "your pawn became a stronger piece. i'll update the guest list."],
		funny: ["a pawn gets a promotion. still has to work here. poor thing."],
	},
	bossTaunt: {
		calm: ["that move certainly happened. i'll give it that.", "so that's the plan. i'll try to look surprised later.", "hm. a choice with consequences. my favorite kind."],
		funny: ["keep making moves like that. i'm almost comfortable.", "all that thinking. let's see what it bought you."],
	},
	// Profile screen: comments on the player's own numbers ({placeholders} are filled by profile.js).
	profileGreet: { calm: ["hey, {name}. {games} games. let's inspect the damage.", "oh, the records room. i'll get my reading glasses. i don't have any.", "{games} games on file. i read them all. twice. for fun, apparently."] },
	profileEmpty: { calm: ["no games yet. very clean record. suspiciously clean.", "nothing to see here. play something and i'll have opinions.", "an empty history. bold strategy. can't lose what you never play."] },
	profileWinRate: { calm: ["you win {rate}% of the time. the other part we don't talk about.", "{rate}% wins. i've seen worse. mostly in mirrors.", "win rate: {rate}%. i'm told numbers don't lie. they do tease, though."] },
	profileFirstMove: { calm: ["{move} again. you really like the classics.", "your favourite start is {move}. i could set a clock by it.", "{move}. every time. your opponents are taking notes. so am i."] },
	profileOpponent: { calm: ["{name} again. {games} games. you two should get matching shirts.", "you've met {name} {games} times. you won {wins}. it's complicated.", "{name}, your favourite rival. or your favourite problem. same thing."] },
	profileAverage: { calm: ["average game: {moves} moves. just enough time to reconsider everything.", "{moves} moves per game. not short, not long. very medium of you.", "{moves} moves on average. somebody likes to take their time."] },
	profileStreakWin: { calm: ["{length} wins in a row. careful, confidence is contagious.", "on a {length}-game winning run. i'll pretend i'm not impressed.", "{length} straight wins. the board is starting to fear you. a little."] },
	profileStreakLoss: { calm: ["{length} losses in a row. the board owes you one. maybe.", "a {length}-game rough patch. even the moon has phases.", "{length} losses straight. consistency picked a bad time."] },
	profilePiece: { calm: ["your {piece} did {count} moves of work. someone give it a day off.", "{piece}: {count} moves. clearly the favourite child.", "you move the {piece} a lot. {count} times. it's fine. i'm not jealous."] },
	profileMode: { calm: ["{count} games in {mode}. you know what you like.", "{mode}: {count} games. comfort zone located.", "mostly {mode}. {count} games of it. variety is overrated anyway."] },
	profileStatGames: { calm: ["{count} games played. the board still hasn't asked for a break.", "{count} games. that's a lot of sitting very still.", "{count} games. i counted. i had time."] },
	profileStatWins: { calm: ["{count} wins. yes, i kept the receipts.", "you won {count}. the receipts are getting a little smug.", "{count} wins. frame them. or don't. i'm not your decorator."] },
	profileStatLosses: { calm: ["{count} losses. even your mistakes get their own little box.", "{count} losses. call them lessons. expensive ones.", "{count} defeats. character building. lots of character."] },
	profileStatDraws: { calm: ["{count} draws. nobody won the argument. familiar.", "{count} ties. very polite. very suspicious.", "{count} draws. everyone shook hands and went home."] },
	profileFormWin: { calm: ["a win. nice. don't let it go to your head.", "that one went well. i remember. i was there. sort of.", "won that one. the rest of the row is jealous."] },
	profileFormLoss: { calm: ["a loss. we don't talk about that one.", "that one stung. it's in the past. right next to the other ones.", "lost that one. the board says sorry. the board is lying."] },
	profileFormDraw: { calm: ["a draw. a very long handshake.", "tied. everybody gets a participation sticker.", "a draw. both of you agreed to be equally tired."] },
	profileRateUp: { calm: ["climbing to {rate}%. look at you go.", "up to {rate}%. the line likes you today.", "{rate}% and rising. don't look down."] },
	profileRateDown: { calm: ["down to {rate}%. gravity works on lines too.", "{rate}%. the line took a little nap.", "dropping to {rate}%. it's called a dip. sounds tastier than it is."] },
	profileGameWin: { calm: ["you beat {rival} there. i'd frame that one.", "a win against {rival}. very satisfying replay material.", "{rival} lost that one. they're probably still thinking about it."] },
	profileGameLoss: { calm: ["{rival} got you there. revenge is a valid hobby.", "lost to {rival}. watch the replay. or don't. i won't tell.", "{rival} won that one. bold of them."] },
	profileGameDraw: { calm: ["a draw with {rival}. call it a long handshake.", "you and {rival} tied. nobody had to be gracious.", "draw with {rival}. perfectly balanced. perfectly unresolved."] },
	profileGameOther: { calm: ["no winner there. just moves. lots of moves.", "that one has no score. the pieces still got their exercise.", "nothing to win there. good for practice. and for my nerves."] },
	profilePoke: { calm: ["yes. still here. the numbers don't need you to poke me.", "poking me won't change the stats. i checked.", "careful. i bruise. emotionally."] },
	bossProfileLosses: {
		calm: ["{partidas} games, no wins against me. shall we revisit the arrangement?"],
		funny: ["{partidas} attempts. at least the rematch button's getting exercise."],
	},
	bossProfileWins: {
		calm: ["{victorias} wins against me. yes, i kept the receipts."],
		funny: ["you've won {victorias} times. i'm calling this one an audit."],
	},
	bossProfileFirstMove: {
		calm: ["{jugada} again? go on. i'd hate to spoil the surprise."],
		funny: ["let me guess. {jugada}. really making me work for this."],
	},
	bossProfilePiece: {
		calm: ["fond of your {pieza}, aren't you? i'll remember where you put it."],
		funny: ["your {pieza} gets all the attention. let's see how it handles mine."],
	},
	bossAgain: {
		calm: [
			"back already. the board barely had time to recover.",
			"a rematch. good. i left a few problems unfinished.",
		],
		funny: [
			"oh, you came back. i'll put on my welcoming expression. done.",
		],
	},
	bossAgainMany: {
		calm: [
			"visit {veces}. familiar board. familiar warning.",
			"game {veces}. still looking for the ending you ordered?",
		],
		funny: [
			"{veces} visits. i'd offer a loyalty card, but you know the reward.",
		],
	},
	bossWaiting: {
		calm: [
			"take your time. the next move will still be waiting.",
			"still thinking? good. i'd hate for this to be an accident.",
			"such a long pause. your king must feel very reassured.",
		],
		funny: [
			"searching for a good move? do let me know if one turns up.",
			"i could nap. but i'd hate to miss the explanation for your next move.",
		],
	},
	bossReturn: {
		calm: [
			"back. good. your position was getting lonely.",
			"the board waited. so did i. one of us had a choice.",
		],
		funny: [
			"get a second opinion? lovely. now two people can worry.",
		],
	},
	bossSleepTalk: {
		calm: [
			"zzz... that square? really?... zzz...",
			"zzz... your turn... unless i take it... zzz...",
		],
		funny: [
			"zzz... nice plan... shame about the ending... zzz...",
		],
	},
	bossDouble: {
		calm: [
			"two moves for me. i do appreciate efficient service.",
			"your turn. after this small correction to the schedule.",
		],
		funny: [
			"another move. must be a problem with the word 'fair.'",
		],
	},
	bossDodge: {
		calm: [
			"you almost took it. almost is doing a lot of work there.",
			"missed. i'd offer it back, but i like this version.",
		],
		funny: [
			"my piece moved out of the way. terribly uncooperative of it.",
		],
	},
	bossRewind: {
		calm: [
			"undoing that. lovely move. wrong audience.",
			"let's put that move back where you found it.",
		],
		funny: [
			"rewound. there. a much more convenient past.",
		],
	},
	bossFlip: {
		calm: [
			"let's swap sides. you seem so pleased with yours.",
			"your pieces are mine now. ownership's such a flexible thing.",
		],
		funny: [
			"swapped our pieces. same board. funny how the mood changed.",
		],
	},
	bossTired: {
		calm: [
			"...hm. apparently bending the rules counts as exercise.",
			"cheating takes energy. very poor design, if you ask me.",
		],
		funny: [
			"just catching my breath. don't look so encouraged.",
		],
	},
	bossExhausted: {
		calm: [
			"one... moment. dramatic pauses cost less than moves.",
			"eyes closed. calculating. yes, that's the official explanation.",
		],
		funny: [
			"if i nod off, try to win quietly. professional courtesy.",
		],
	},
	bossAsleep: {
		calm: [
			"zzz... still winning... probably...",
			"zzz... leave the pieces... i'll count them... zzz...",
		],
		funny: [
			"zzz... your king's trapped... i win... (even his dreams bend the rules)",
		],
	},
	bossVolume: {
		calm: [
			"that's quiet enough. some warnings deserve an audience.",
			"lower the volume. the next move still lands.",
		],
		funny: [
			"trying to soften the soundtrack? the board didn't get the memo.",
		],
	},
	greeting: {
		calm: [
			"hey. i'm Alfi, the bishop. pick a mode. i'll supervise. sitting down, ideally.",
			"welcome to ArchChess. i brought advice. you brought the liability.",
			"hey. bishops move diagonally. even my shortcuts take an angle.",
			"fresh board. all the pieces still trust us. enjoy that.",
			"back again. practice, or shall we involve a witness?",
			"sixty-four squares. apparently that wasn't enough room for an easy game.",
			"hey. i'll comment on your moves. yes, even that one.",
		],
		funny: [
			"looks like you're about to play chess. i'll alert the pieces.",
			"i'm your helpful mascot. the 'helpful' part is on a flexible schedule.",
			"you get a bishop with opinions. premium service. questionable value.",
			"i stay on one square color. very committed to half the job.",
			"lots of chess advice. no hands. excellent hiring decision.",
			"take your time. i'm already doing that professionally.",
			"looking for a knight? funny little horse. questionable steering.",
		],
	},
	idle: {
		calm: [
			"aim pieces at the center. more choices. a terrible burden, i know.",
			"castle early: move king and rook together to shelter the king. royalty needs a break.",
			"use the left and right arrow keys to review moves. hindsight finally gets a job.",
			"bring knights and bishops out before the queen. let someone else do the errands.",
			"pawns can't move backward. excellent feature for creating regrets.",
			"a rook near the far edge can attack pawns along that row. rude houseguest.",
			"find a better square for your least useful piece. give its nap some purpose.",
			"count attackers and defenders before taking a piece. yes, there's homework.",
			"rooks like columns with no pawns in the way. big fans of doing less walking.",
			"look for attacks on the king, captures, then other threats. a little to-do list.",
			"ask what your opponent wants next. other people. always complicating things.",
			"a knight safe from enemy pawns can be a nuisance deep in their half. nice parking.",
			"two bishops like clear diagonal paths. we get fussy about traffic.",
			"when few pieces remain, a king can help attack. finally, some actual work.",
			"two friendly pawns in one column can be hard to defend. ambitious filing system.",
			"a pawn with no friendly pawns in neighboring columns may need piece support. needy.",
			"no enemy pawns ahead to block or capture yours? help it cross. ambition needs backup.",
			"try bringing out a new piece before moving the same one again. share the workload.",
			"opposing bishops on different square colors often mean a tie with few pieces left. cozy.",
			"with mostly pawns left, kings facing across one empty square can force one aside.",
			"put a rook behind a pawn racing to the far edge. even ambition needs supervision.",
			"king and two knights can't force an inescapable attack on a lone king. teamwork, huh.",
			"no legal moves, king safe? stalemate: a draw, so nobody wins. watch that when ahead.",
			"castling moves king and rook. king can't start, cross or land under attack. picky trip.",
			"each pawn move changes the squares it protects. no return policy.",
			"ahead in pieces? trades can simplify the game. behind? try making trouble.",
		],
		funny: [
			"knights reach fewer squares from the edge. great seats. terrible job prospects.",
			"two bishops, clear diagonals. finally, colleagues who respect personal space.",
			"reading a chess guide. the chapter on waiting is taking forever.",
			"bishops stay on one square color. i call that a boundary. very healthy.",
			"i'd study every possible first move. but then when would i put it off?",
			"nice soundtrack. really adds dignity to the free pieces.",
			"diagonal travel only. i don't cut corners. physically can't.",
			"the queen moves along rows, columns, and diagonals. bit of an overachiever.",
			"chess has a lot of opening books. apparently 'just start' needed several volumes.",
			"rooks move straight. bishops move diagonally. knights took a less direct course.",
			"pawns do the hard work. royalty does the standing nearby. classic management.",
			"giving away a piece can work if it wins something better. lovely use of 'if.'",
			"i made a chess plan for today. step one: wait for you to make a chess plan.",
			"we could play all day. bold of you to think i had other plans.",
			"i was first onto this screen. still waiting for my participation trophy.",
			"every legal move makes things worse? that's called zugzwang. even waiting quit.",
			"so many possible games. yet here i am, worrying about this one.",
			"you can play without a mascot. terrible news for my performance review.",
			"defend before the attack arrives. ruins their whole surprise party.",
			"taking a piece just removes it. the exit interview happens offscreen.",
			"being a bishop is simple. pick a diagonal. develop opinions about everyone else.",
		],
	},
	start: {
		calm: [
			"here we go. try to keep some pieces for later.",
			"board's ready. no pressure. just thirty-two witnesses.",
			"white moves first. black gets a moment to judge.",
			"new game. use the center, bring pieces out, shelter your king. casual little list.",
			"everything's in place. suspiciously peaceful.",
			"game on. a plan would be a charming addition.",
		],
		funny: [
			"new game. fresh chances to call a mistake 'experimental.'",
			"all the pieces are home. enjoy the group photo.",
			"black gets the second move. really milking that thinking time.",
			"thirty-two pieces. let's see who makes it to the closing credits.",
			"deep breath. the board can smell a rushed queen move. probably.",
		],
	},
	waiting: {
		calm: [
			"room made. copy the invitation and send it to your opponent. i can't lick stamps.",
			"waiting for your opponent. we'll start when they join. radical system.",
			"board's ready. slight shortage of opponent.",
			"plan your first few moves while we wait. or rehearse looking confident.",
			"your opponent isn't here yet. the pieces have negotiated a break.",
		],
		funny: [
			"will they join? thrilling stuff. i may sit down harder.",
			"they're probably studying. very rude to prepare for a friendly game.",
			"waiting... the one part of chess i've fully mastered.",
			"counted the squares. sixty-four. i'll demand a recount.",
			"no opponent yet. your undefeated run is looking excellent.",
			"the rooks are asleep. honestly, solid use of the wait.",
			"polished the board. now the mistakes will really shine.",
			"they may be looking for Wi-Fi. a fearsome opponent in its own right.",
			"no one to play yet. finally, a threat your king can handle.",
			"asked the knight for directions. got two turns and a headache.",
			"calling this 'strategic patience' makes it look like i'm working.",
			"copied the invitation? even an opponent needs directions to their problems.",
			"been waiting so long i nearly opened a chess book. close call.",
			"very quiet room. i can hear a pawn reconsidering its career.",
		],
	},
	opponentJoined: {
		calm: [
			"your opponent's here. time to make the waiting look productive.",
			"opponent connected. there goes the peace and quiet.",
			"both here. good luck. i'll keep my enthusiasm at a safe level.",
		],
		funny: [
			"finally. nearly had to pretend i knew how to use a mouse.",
			"your opponent arrived. a firm imaginary handshake should do.",
			"opponent on board. the pawns would like to discuss their contracts.",
		],
	},
	"theme-classic": {
		calm: ["classic theme. the board has put on its sensible shoes.", "back to basics. my wardrobe budget survives another day."],
		funny: ["classic. these squares were making mistakes before it was fashionable.", "no costume. apparently a talking bishop is enough of a look."],
	},
	"theme-warm": {
		calm: ["warm colors. very cozy place to question a move.", "warm theme. the board looks ready for a nap. excellent taste."],
		funny: ["woolly hat on. thinking temperature remains unchanged.", "hot chocolate colors. please avoid dunking the pawns."],
	},
	"theme-cold": {
		calm: ["cold theme. now the board can give your moves a chilly reception.", "cool blues. the thinking is still your department."],
		funny: ["scarf on. diagonals can get drafty.", "frosty board. your king's looking forward to cold feet."],
	},
	"theme-gothic": {
		calm: ["gothic theme. even the squares have something to brood about.", "gothic. very dramatic setting for moving a pawn one square."],
		funny: ["top hat on. my bad advice now looks expensive.", "dark decor. the king's calling his mistakes 'tragic foreshadowing.'"],
	},
	"theme-medieval": {
		calm: ["medieval theme. finally, a board my hat understands.", "medieval. same royal problems. fewer indoor bathrooms."],
		funny: ["helmet on. excellent protection from falling expectations.", "ready for battle. emotionally, i'm still at breakfast."],
	},
	"theme-halloween": {
		calm: ["halloween. the pieces dressed up as people with a plan.", "pumpkins on the board. at least something here has a light inside."],
		funny: ["witch hat on. sadly, turning a bad move into a good one costs extra.", "spooky theme. still less frightening than a queen left undefended."],
	},
	"theme-christmas": {
		calm: ["christmas theme. season's greetings. the pawns asked for time off.", "festive board. hope the king remembered everyone else's presents."],
		funny: ["santa hat on. yes, i'm checking the moves twice.", "holiday spirit. let's generously give the opponent absolutely nothing."],
	},
	"theme-python": {
		calm: ["python theme. even the squares lined up neatly. show-offs.", "python. the snake has better posture than i do."],
		funny: ["cap on. very casual uniform for a very opinionated bishop.", "snake theme. please stop feeding it undefended pawns."],
	},
	"theme-java": {
		calm: ["java theme. the board's awake. must be the coffee.", "java. finally, a theme with a sensible beverage policy."],
		funny: ["coffee cup acquired. productivity remains a rumor.", "java. i'll have a quiet game with an extra shot of hindsight."],
	},
	"theme-warhammer": {
		calm: [
			"Warhammer 40k. blue space soldiers, purple alien bugs. subtle little disagreement.",
			"blue armor versus hungry purple aliens. lovely basis for a board game.",
		],
		funny: [
			"my favorite theme. see? this is me visibly containing my excitement.",
			"helmet's too big. leaves room for all my modest expectations.",
			"unpainted miniatures at home. painted army here. incredible work ethic.",
		],
	},
	poked: {
		calm: [
			"hey. found the bishop. detective work complete.",
			"heard you. the first click had excellent coverage.",
			"keep poking. i charge extra for moving out of reach.",
		],
		funny: [
			"yes, i respond to clicks. career's going beautifully.",
			"another click. i'll add it to your very detailed invoice.",
			"all this poking. you know the pieces move too, right?",
		],
	},
	wakeUp: {
		calm: [
			"huh? resting my eyes. extremely thorough about it.",
			"oh, we're playing. i'll put my awake face on.",
			"brief nap. did the pieces sort it out without us?",
		],
		funny: [
			"dreamed about chess. can't even get a day off in there.",
			"five more minutes... fine. i'll sleep through the exciting bits later.",
			"sleeping upright. finally, a bishop skill with practical value.",
		],
	},
	loading: {
		calm: ["preparing the board... sixty-four squares. ambitious.", "setting up pieces... royalty's demanding the usual seats.", "opening the box... everyone act surprised.", "lining up pawns... morale seems cautiously wooden."],
		funny: [
			"setting up. any missing pawn is on a well-earned break.",
			"polishing the crown. priorities, apparently.",
			"waking the rooks. no solidarity in this workplace.",
			"finding the knight under the sofa. creative route, as usual.",
		],
	},
	error: {
		calm: [
			"server couldn't handle that. try another mode. i'll make a concerned face.",
			"couldn't reach the server. try again. it may be taking my advice on naps.",
		],
		funny: [
			"server left without a goodbye. bold social strategy.",
			"connection failed. impressive defense. can't get a single move through.",
			"server trouble. apparently the pieces weren't the only things under pressure.",
		],
	},

	openingCenter: {
		calm: [
			"{from} to {to}. straight to the center. suspiciously sensible.",
			"starting in the center. look at us, following instructions.",
			"pawn to {to}. room in the middle. even the bishops might get out of bed.",
			"first move into the center. the textbooks can unclench now.",
		],
		funny: [
			"pawn to the center. only a few million people thought of that first.",
			"center first. boring in that annoyingly useful way.",
			"pawn to {to}. wow. the center must be terrified.",
			"straight to the middle. confidence looks lovely before move two.",
		],
	},
	openingWeird: {
		calm: [
			"unusual start. i'll pencil in a question mark. lightly.",
			"starting at the side. giving the center some personal space, i see.",
		],
		funny: [
			"{to}? sure. let's give the textbooks the afternoon off.",
			"experimental start. excellent. a polite word for all sorts of things.",
			"bold first move. we'll decide what kind of bold later.",
			"letting them take the center. very generous. let's hope there's a second step.",
		],
	},
	openingOther: {
		calm: [
			"first move made. we've narrowed it down to a very large number of problems.",
			"pieces starting to move. there goes the tidy bit.",
			"first move. the board has accepted your opening argument.",
		],
		funny: [
			"one move in. no disasters. i'm trying not to get attached.",
			"move one. practically finished, if you ignore the rest.",
		],
	},
	earlyQueen: {
		calm: [
			"queen out early? rescuing her can waste moves. she's very high maintenance.",
			"an early queen gives their pieces a target. thoughtful of her.",
			"they can bring pieces out while attacking your queen. free errands for them.",
		],
		funny: [
			"{ply} moves in and the queen's out. management's doing a site visit.",
			"queen to {to} already. the pawns didn't even get a welcome meeting.",
			"queen's out before the knights. big fan of supervising an empty office.",
			"queen to {to}. us bishops are still home. invitation must be in the mail.",
		],
	},

	castle: {
		calm: [
			"castling: king and rook move together. king gets shelter. rook gets a job.",
			"king tucked away, rook ready. nice. delegation with extra steps.",
			"king and rook relocated. clear the back row so the rooks can protect each other.",
			"king takes shelter. very sensible. hardly royal behavior at all.",
		],
		funny: [
			"king takes cover, rook goes to work. inspiring leadership.",
			"king and rook in one move. the only approved shortcut on this board.",
			"king settled in. presumably waiting for room service.",
		],
	},
	kingWalk: {
		calm: [
			"king's out for a walk. do watch the attacks. scenery's rather hostile.",
			"king to {to}. busy board for someone who can't afford to be caught.",
			"king moving out? watch columns with no pawns. enemy rooks enjoy clear views.",
		],
		funny: [
			"king to {to}. royal sightseeing. terrible travel insurance.",
			"king's out among the enemy pieces. subtle way to request a bodyguard.",
			"king popped out for a snack. the enemy pieces offer free delivery.",
			"king to {to}. brave. also the word people use before 'rescue mission.'",
		],
	},
	bongcloud: {
		calm: [
			"king out this early. the rest of the army must be thrilled to watch.",
			"king to {to}. moving it rules out castling, the king-and-rook shelter move. bold trade.",
		],
		funny: [
			"early king stroll. very efficient way to become everyone's problem.",
			"walking the king out already. who needs shelter when you've got optimism?",
			"king to {to}. the textbook would like a quiet word.",
		],
	},
	knightRim: {
		calm: [
			"knight to {to}. fewer reachable squares at the edge. cozy, if unhelpful.",
			"from {to}, that knight reaches fewer squares than from the center. scenic downgrade.",
		],
		funny: [
			"knight at the edge. fewer options. finally, a relatable piece.",
			"knight to {to}. facing the wall. must be thinking about what it did.",
			"knight to {to}. looking for an exit? can't blame the commute.",
		],
	},
	fianchetto: {
		calm: [
			"bishop to {to}, near the side. long diagonal view. modest taste in real estate.",
			"bishop to {to}. eyeing that long diagonal. i approve with minimal enthusiasm.",
			"bishop tucked behind a side pawn, watching diagonally. working from home.",
		],
		funny: [
			"bishop by the side, long diagonal ahead. excuse me while i quietly care.",
			"bishop to {to}. nice view. shame about all the people trying to take it.",
		],
	},
	pawnRush: {
		calm: [
			"pawn on {to}. getting close to the far edge. suddenly everyone notices the intern.",
			"near the far edge, a pawn can become a new piece. that's how they keep you motivated.",
			"pawn to {to}. closer to the far edge and a job with a better hat.",
			"that pawn's nearly across. check what can stop it before ordering the crown.",
		],
		funny: [
			"pawn on {to} wants the far edge and a new job. relatable ambitions.",
			"pawn to {to}. big queen energy for someone with a one-way commute.",
			"pawn on {to} is rehearsing a speech. far edge first, royal ambitions later.",
		],
	},
	repeat: {
		calm: [
			"same position three times can allow a draw: nobody wins. efficient recycling.",
			"pieces going back and forth. a draw means nobody wins. anyone feeling diplomatic?",
		],
		funny: [
			"forward, back, forward... the board's a dance floor with worse shoes.",
			"this looks familiar. apparently we're paying rent on these squares.",
			"another repeat. great. the pieces have found a routine they won't improve on.",
		],
	},

	capture: {
		calm: [
			"piece taken. very tidy. except for the bit where someone lost a piece.",
			"one enemy piece gone. their staffing situation just got interesting.",
			"big piece taken. that should make their planning pleasantly awkward.",
			"capture on {to}. that square has a strict one-guest policy.",
			"{victima} gone. somebody's plan just lost a key employee.",
		],
		funny: [
			"piece removed. i'll skip the dramatic chewing noises.",
			"one piece off the board. no animation budget for a farewell tour.",
			"{victima} taken on {to}. apparently the seat was reserved.",
			"piece taken on {to}. lovely. even the board's decluttering.",
		],
	},
	minorWon: {
		calm: [
			"{victima} off the board. its schedule just opened right up.",
			"nice capture. one less piece offering you unsolicited problems.",
			"piece taken. useful. now comes the small matter of the rest of the game.",
		],
		funny: [
			"pocketing that {victima}. very small trophy room we're building.",
			"one less enemy knight or bishop. i approve of half those possibilities.",
			"{victima} caught on {to}. terrible place to stand, in hindsight.",
		],
	},
	bishopLost: {
		calm: [
			"bishop gone. i'll observe a moment of highly professional sulking.",
			"bishop taken on {to}. that diagonal feels underqualified now.",
		],
		funny: [
			"a bishop. really. was there nothing less stylish available?",
			"colleague taken. i'll be filing a diagonally worded complaint.",
			"one less bishop. somehow this wasn't covered in the benefits package.",
			"bishop down on {to}. i'm requesting compassionate nap leave.",
		],
	},
	trade: {
		calm: [
			"equal-value pieces traded. fewer pieces, same amount of worrying.",
			"one piece for another. wonderfully brief negotiations.",
			"trade on {to}. fewer pieces to supervise. i support this.",
			"similar pieces exchanged. the balance keeps its very boring job.",
		],
		funny: [
			"you take one, they take one. cooperative cleaning with trust issues.",
			"trading on {to}. receipt optional. regret sold separately.",
		],
	},
	enPassant: {
		calm: [
			"en passant: your pawn catches a pawn that just dashed two squares past it. fast service.",
			"a pawn just moved two squares beside yours? take it as if it moved one. now or never.",
		],
		funny: [
			"pawn rushed two squares past yours and got taken anyway. express checkout.",
			"a pawn gets caught just after its two-square dash. even shortcuts have tolls.",
			"en passant: catch the pawn that just dashed two squares past yours. fine print bites.",
		],
	},
	queenLost: {
		calm: [
			"queen gone. the job description just expanded for everyone else.",
			"no queen. well, the king did say he wanted a quieter life.",
			"queen taken on {to}. regroup. preferably before their next bright idea.",
		],
		funny: [
			"queen's gone. that's going to be a very awkward royal announcement.",
			"queen down on {to}. the crown's suddenly a much less attractive benefit.",
			"no queen. apparently we selected the deluxe difficulty package.",
		],
	},
	queenWon: {
		calm: [
			"their queen's gone. nice. i'll permit a modest eyebrow raise.",
			"you took their queen. that should put a small dent in their afternoon.",
			"queen won. if you're ahead, trading pieces can help. stay awake, though.",
		],
		funny: [
			"queen captured. the royal complaints department is going to be busy.",
			"queen taken on {to}. the other king would like to renegotiate the game.",
			"a queen's worth about nine pawns. very efficient shopping.",
		],
	},
	blunder: {
		calm: [],
		funny: [
			"wow. that move really cleared some space for the opponent.",
			"bold move. left the good squares for someone else.",
			"brilliant. two question marks mean a bad move in chess. even the punctuation has doubts.",
			"all part of the plan. looking forward to meeting the plan.",
			"gave a piece away. very generous. let's try a less expensive hobby within the hobby.",
			"i'll write that move down. under 'interesting expenses.'",
			"free piece. i'm sure the opponent will appreciate your outreach program.",
			"even the computer paused. probably checking whether that was a donation.",
			"such generosity. your pieces may want to revise their contracts.",
			"that move belongs in a lesson. very close to the word 'avoid.'",
			"hm. let me check that... yes. still unfortunate.",
			"that piece was just getting comfortable. brief career.",
			"interesting move. i'll let that word do some heavy lifting.",
			"rough move. happens. let's give the next one a slightly longer interview.",
		],
	},

	check: {
		calm: [
			"check. king under attack. terrible time to ignore a notification.",
			"check means the king's under attack. royal inconvenience has arrived.",
			"king under attack. get it safe. the royal nap can wait.",
			"check: king under attack. block it, take the attacker, or move the king. pick a legal fix.",
			"attack from {to}. king's in check. that's its afternoon sorted.",
		],
		funny: [
			"check. king under attack. wishing harder remains an unapproved defense.",
			"king under attack. someone's taken the open-door policy too literally.",
			"check. king under attack. there goes the royal coffee break.",
			"your majesty, an attack. yes, i'll mark it urgent. again.",
		],
	},
	checkAgain: {
		calm: ["another attack on the king. very persistent customer service.", "king under attack again. lovely day for running in expensive shoes.", "another check: the king's attacked. peace talks seem to be going well."],
		funny: [
			"king attacked again. perhaps leave the suitcase packed.",
			"another attack. the king would like to unsubscribe.",
		],
	},
	promotion: {
		calm: [
			"pawn reached the far edge and became a new piece. career advice actually worked.",
			"pawn at the far edge gets a new piece. finally, a promotion with useful benefits.",
			"pawn promoted on {to}: a stronger piece now. same employee, bigger problems.",
		],
		funny: [
			"pawn reached the far edge. could become a queen. apparently seniority isn't required.",
			"far edge reached. pawn gets a better job. i should ask about vacancies.",
			"pawn made it to {to}. i always believed in it. quietly. very quietly.",
		],
	},
	bigLead: {
		calm: [
			"{bando} has the stronger army left. lovely. still have to finish the game.",
			"{bando} has more strength left on the board. subtle little hint about who's ahead.",
			"{bando} has the stronger pieces left. steady now. heroics have terrible hours.",
		],
		funny: [
			"{bando} has the stronger army. the other side's leaning on 'technically alive.'",
			"{bando} has more strength left. the other army should have read the return policy.",
		],
	},
	comeback: {
		calm: [
			"the advantage changed sides. rude of it not to leave a note.",
			"game's turned around. hope nobody already wrote the victory speech.",
			"{bando} is ahead now. apparently the old plan had an expiry date.",
		],
		funny: [
			"plot twist. how inconsiderate. i was nearly comfortable.",
			"comeback. always nice when a game remembers to have a second act.",
			"{bando} turned the tables. ambitious, considering we were using that board.",
		],
	},
	milestone: {
		calm: [
			"{moves} moves. look at all that thinking we've committed to.",
			"move {moves}. i'd look more serious, but this is my only face.",
			"{moves} moves. check the plan still exists. they wander off.",
		],
		funny: [
			"{moves} moves. well past the part where looking confident counts as preparation.",
			"move {moves}. coffee's getting cold. the board remains inconveniently awake.",
		],
	},
	longGame: {
		calm: [
			"long game. stay focused. i'll provide the illusion of enthusiasm.",
			"lots of moves now. even a quiet move deserves a look. terrible news for my nap.",
		],
		funny: [
			"50 moves each, no pawn move or capture? a draw can be claimed. nobody wins. quite a break.",
			"still playing. lovely. my imaginary lunch has gone imaginary cold.",
		],
	},
	quickMate: {
		calm: ["quick checkmate: king attacked, no legal escape. efficient. rather abrupt.", "king attacked with no escape. quick finish. review the first moves at a slower pace."],
		funny: [
			"king attacked and trapped already. well. that saved us a whole afternoon.",
			"checkmate: king attacked, no legal escape. barely got my commentary chair warm.",
		],
	},
	victory: {
		calm: [
			"you won. nice work. there, my daily burst of enthusiasm.",
			"game won. try to look like you expected that.",
			"checkmate: king attacked, no legal escape. tidy finish. i'll allow a little smugness.",
			"you won. review what worked before we accidentally call it a strategy.",
		],
		funny: [
			"victory. i'd throw confetti, but i'd also have to clean it up.",
			"their king's trapped under attack. lovely. the crown doubles as a paperweight now.",
			"you won. i'll tell the pieces your management style paid off.",
			"victory. i'm framing the game. very small frame. budget reasons.",
		],
	},
	defeat: {
		calm: [
			"game lost. the rematch button seems annoyingly optimistic.",
			"lost this one. new game, slightly wiser problems.",
			"king trapped under attack. game over. review where it changed. hindsight's free.",
			"losses teach things too. check where the game turned. might as well use the tuition.",
		],
		funny: [
			"well. the opponent had a good day. terribly inconsiderate.",
			"defeat. lots to learn. very educational. perhaps too educational.",
			"your king's trapped under attack. i'll pretend i was looking at a different diagonal.",
		],
	},
	draw: {
		calm: ["draw. nobody wins. a very diplomatic use of everyone's afternoon.", "a tie. both sides have successfully avoided the victory speech.", "draw. neither side gets the last word. except me, apparently."],
		funny: [
			"draw. all that tension, shared custody of the result.",
			"draw. nobody wins. remarkably fair. i find it suspicious.",
			"a tie. the pieces can finally stop pretending they're comfortable.",
		],
	},
};

export const themeLines = {
	warhammer: {
		greeting: {
			calm: [
				"hey. blue armored soldiers versus purple alien bugs. pick a side. pack a snack.",
				"helmet's been on since breakfast. tactical decision. definitely not stuck.",
			],
			funny: [
				"future warfare, tiny squares. apparently the universe also has a budget.",
				"three months painting one pawn. very efficient if you don't count time.",
			],
		},
		idle: {
			calm: [
				"Tyranids are aliens that eat planets. i'd start with lunch, but i'm no expert.",
				"space marines are giant armored soldiers. subtlety apparently costs extra.",
				"a Dreadnought is a warrior in a robot. ours is a rook. very direct career move.",
			],
			funny: [
				"could paint all sixty-four squares by hand. could also have a nice afternoon.",
				"all this armor. still no pockets. military spending is out of control.",
				"more miniatures than socks. excellent collection. questionable laundry plan.",
			],
		},
		start: {
			calm: ["battle begins. everyone look busy. especially the royalty."],
			funny: ["to your squares. inspiring speech complete. saved us twenty minutes."],
		},
		capture: {
			calm: ["casualty on {to}. someone update the very small memorial wall.", "one less piece in the battle. suddenly there's room for tactics. or a chair."],
			funny: [
				"piece taken. the hungry alien swarm calls that catering.",
				"{victima} gone. the official report will call that 'unexpected downsizing.'",
			],
		},
		check: {
			calm: ["king under attack. lovely. command finally gets some field experience."],
			funny: ["check means king under attack. red alert. i'll use my concerned helmet."],
		},
		promotion: {
			calm: ["pawn reaches the far edge, becomes a new piece. promotion approved. paperwork survived."],
			funny: ["pawn reached the far edge and got a new piece. armor upgrade. same terrible hours."],
		},
		victory: {
			calm: ["victory. a glorious chapter. i'll write the short version."],
			funny: ["won the battle. i'll accept a medal, provided someone else carries it."],
		},
		defeat: {
			calm: ["battle lost. regroup. ideally somewhere with fewer hungry aliens."],
			funny: ["the swarm won. rematch? i'll pack fewer snacks and more shields."],
		},
		poked: {
			calm: ["careful with the helmet. fresh paint is my strongest defense."],
			funny: ["yes, the armor responds to poking. very advanced military technology."],
		},
	},
};
