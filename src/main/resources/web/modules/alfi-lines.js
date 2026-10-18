// Everything Alfi can say. Each kind has calm lines (the default) and funny
// ones used only now and then. Every line is about chess: rules, theory,
// famous games and players, or chess video games. {to}, {from}, {pieza},
// {victima}, {bando}, {ply} and {moves} are filled in from the move.
export const alfiLines = {
  greeting: {
    calm: [
      "¡Hola! Soy Alfi, el alfil. Elige un modo y empezamos.",
      "Bienvenido a ArchChess. ¿Echamos una partida?",
      "Hola. Yo solo me muevo en diagonal, pero te acompaño en todas direcciones.",
      "¡Buenas! El tablero está limpio y las piezas, deseando moverse.",
      "Hola de nuevo. ¿Práctica tranquila o rival de carne y hueso?",
      "Bienvenido. Sesenta y cuatro casillas y infinitas partidas por delante.",
      "¡Hola! Prometo comentar tus jugadas. Las buenas y las otras.",
    ],
    funny: [
      "Parece que intentas jugar al ajedrez. ¿Quieres ayuda?",
      "Mi abuelo era un clip de oficina. Yo prefiero las diagonales.",
      "Kasparov tenía a Deep Blue. Tú me tienes a mí. Sales ganando.",
      "Soy un alfil de casillas blancas. No me pidas que pise las negras.",
      "Hola. Tengo 400 años de teoría en la cabeza y ninguna mano para mover.",
      "Bienvenido. Aquí no hay reloj, así que puedes pensar como Petrosian.",
      "Hola. Si buscas al caballo, está en el borde. Como siempre.",
    ],
  },
  idle: {
    calm: [
      "Consejo: controla el centro del tablero.",
      "Enroca pronto para proteger a tu rey.",
      "Usa ← y → para repasar la partida.",
      "Desarrolla caballos y alfiles antes de sacar la dama.",
      "Los peones no retroceden: piensa antes de empujarlos.",
      "Una torre en séptima fila suele ser una pesadilla para el rival.",
      "Si no sabes qué mover, mejora tu peor pieza.",
      "Cuenta atacantes y defensores antes de capturar.",
      "Torres en columnas abiertas. Siempre.",
      "Busca jaques, capturas y amenazas, en ese orden.",
      "Antes de mover, pregúntate qué quiere hacer tu rival.",
      "Un caballo en una casilla avanzada que no pueden echar los peones vale oro.",
      "La pareja de alfiles brilla en posiciones abiertas.",
      "En los finales, el rey es una pieza de ataque. ¡Actívalo!",
      "Los peones doblados suelen ser una debilidad.",
      "Un peón aislado necesita piezas que lo defiendan.",
      "Peón pasado: hay que empujarlo, decía Nimzowitsch.",
      "No muevas la misma pieza dos veces en la apertura sin motivo.",
      "Con alfiles de distinto color, los finales tienden a las tablas.",
      "En los finales de peones, la oposición lo decide casi todo.",
      "Coloca las torres detrás de los peones pasados.",
      "Dos caballos solos no pueden forzar el mate.",
      "El ahogado es tablas. Ojo cuando vayas muy por delante.",
      "No puedes enrocar si el rey está en jaque o pasa por una casilla atacada.",
      "Cada jugada de peón deja casillas débiles detrás.",
      "Si tu posición es peor, busca complicaciones. Si es mejor, simplifica.",
    ],
    funny: [
      "Tarrasch decía que un caballo en el borde es un caballo triste. Tenía razón.",
      "Dos alfiles juntos son una pareja peligrosa. Lo digo por experiencia.",
      "Estoy leyendo «Mi sistema» de Nimzowitsch. Va por el capítulo del bloqueo.",
      "Dato: el alfil se llamaba «al-fil», elefante en árabe. Respeta mi linaje.",
      "Capablanca casi nunca estudiaba aperturas. No lo intentes en casa.",
      "Morphy ganó la Partida de la Ópera mientras sonaba una ópera. Aquí suena chiptune.",
      "En el ajedrez antiguo yo saltaba dos casillas en diagonal. Eran otros tiempos.",
      "La dama antes solo movía una casilla. En la Valencia del siglo XV le dieron superpoderes.",
      "La Ruy López la estudió un cura extremeño en 1561. El ajedrez también es cosa nuestra.",
      "Lucena publicó su libro en Salamanca en 1497. Su posición sigue enseñando torres.",
      "Philidor decía que los peones son el alma del ajedrez. Los alfiles somos el estilo.",
      "Tal, el mago de Riga, sacrificaba piezas como quien reparte caramelos.",
      "Fischer tenía 13 años cuando jugó «la partida del siglo». Yo tenía menos de un año.",
      "La partida más larga posible tiene miles de jugadas. No me tientes.",
      "Steinitz fue el primer campeón del mundo, en 1886. Yo fui el primero en llegar a esta pantalla.",
      "Zugzwang: cuando cualquier jugada empeora tu posición. Como los lunes.",
      "¿Sabías que hay más partidas posibles que átomos en el universo? Yo me sé tres.",
      "Judit Polgar llegó a estar entre las diez mejores del mundo. Y sin mascota.",
      "Petrosian se defendía antes de que le atacaran. Muy precavido el hombre.",
      "En Battle Chess las piezas se pelean. Aquí somos más civilizados. Casi.",
      "Ser alfil es fácil: vas en diagonal y nunca cambias de color.",
    ],
  },
  start: {
    calm: [
      "¡Que empiece la partida!",
      "Tablero listo. ¡Suerte!",
      "Piezas colocadas. Las blancas abren el baile.",
      "Nueva partida. Centro, desarrollo y rey a salvo.",
      "Todo en su sitio. Tu turno de escribir historia.",
      "Partida en marcha. Que gane el mejor plan.",
    ],
    funny: [
      "Nueva partida, nuevas oportunidades de dejarse una pieza. ¡Ánimo!",
      "Las piezas están en su sitio. Por ahora.",
      "Las negras ya están pensando en la Siciliana. Lo noto.",
      "Treinta y dos piezas en el tablero. Apuesto a que no acaban todas.",
      "Respira hondo. Ni Carlsen gana todas.",
    ],
  },
  // PvP room created, rival not there yet. Alfi keeps chatting while waiting.
  waiting: {
    calm: [
      "Sala creada. Copia la invitación y mándasela a tu rival.",
      "Esperando a tu oponente. La partida empezará cuando se una.",
      "El tablero está listo. Solo falta alguien al otro lado.",
      "Mientras esperas: repasa tu plan de apertura.",
      "Tu rival aún no ha llegado. Las piezas no se mueven hasta entonces.",
    ],
    funny: [
      "¿Vendrá? ¿No vendrá? Esto tiene más suspense que un final de torres.",
      "Tu rival está tardando. Seguro que está repasando la teoría a escondidas.",
      "Esperando… Como Fischer antes del match de 1972. Ojalá este sí aparezca.",
      "Mientras tanto, cuento las casillas. Sesenta y cuatro. Otra vez sesenta y cuatro.",
      "Si tarda mucho, ¿cuenta como abandono? Pregunto para un amigo.",
      "Las torres se están quedando dormidas. Que alguien despierte a tu rival.",
      "He limpiado el tablero tres veces. Ya brilla más que una dama recién coronada.",
      "Tu oponente estará buscando la Wi-Fi. Pasa en las mejores familias.",
      "En los torneos el reloj ya estaría corriendo. Aquí somos más pacientes.",
      "Le he preguntado al caballo si viene alguien. Me ha contestado en forma de L.",
      "Esperar también es estrategia. Petrosian lo llamaría profilaxis.",
      "¿Has copiado la invitación? Sin ella tu rival no encontrará el tablero.",
      "Llevo tanto rato esperando que ya me sé de memoria la Najdorf.",
      "Silencio en la sala. Solo se oye a un peón respirando.",
    ],
  },
  opponentJoined: {
    calm: [
      "¡Tu rival ha llegado! Que empiece la partida.",
      "Oponente conectado. Ya podéis jugar.",
      "Ya estáis los dos. ¡Suerte!",
    ],
    funny: [
      "¡Por fin! Pensaba que tendría que jugar yo contra ti.",
      "Ha llegado tu rival. Daos la mano. Bueno, como podáis.",
      "Rival a bordo. Que tiemblen los peones.",
    ],
  },
  // Board and piece theme changed: Alfi changes outfit too.
  "theme-classic": {
    calm: ["Tema clásico. Lo de siempre nunca falla.", "Vuelta a los orígenes. Me sienta bien el blanco."],
    funny: ["Clásico. Como un Gambito de Dama bien jugado.", "Ropa de diario otra vez. Se acabó el carnaval."],
  },
  "theme-warm": {
    calm: ["Tema cálido. Más acogedor para pensar.", "Tonos cálidos. Partida de sobremesa."],
    funny: ["Qué calorcito. Me pido la casilla junto a la chimenea.", "Cálido. Ideal para una partida con chocolate y churros."],
  },
  "theme-cold": {
    calm: ["Tema frío. Cabeza fría, jugadas precisas.", "Azules fríos. Concentración total."],
    funny: ["Brrr. Alguien que le traiga una bufanda al rey.", "Frío. Como el cálculo de Stockfish."],
  },
  "theme-gothic": {
    calm: ["Tema gótico. Elegante y sombrío.", "Gótico. Las catedrales también tienen alfiles. Bueno, casi."],
    funny: ["Gótico. Me he puesto mi túnica más dramática.", "Tablero gótico. El rey ya está escribiendo poesía triste."],
  },
  "theme-medieval": {
    calm: ["Tema medieval. Como en tiempos de Alfonso X y su libro del ajedrez.", "Medieval. En aquella época yo saltaba dos casillas."],
    funny: ["Medieval. ¿Dónde he dejado mi armadura?", "¡A las armas! Bueno, a las diagonales."],
  },
  "theme-halloween": {
    calm: ["Tema de Halloween. Partida de miedo.", "Halloween. Calabazas en el tablero."],
    funny: ["¡Buu! Hoy voy disfrazado de alfil. Como siempre, pero con miedo.", "Halloween. Lo más terrorífico sigue siendo un mate del pastor."],
  },
  "theme-christmas": {
    calm: ["Tema navideño. Felices fiestas sobre el tablero.", "Navidad. Que te toquen dos alfiles en el sorteo."],
    funny: ["Navidad. Yo quiero una dama para Reyes.", "Jo, jo, jo. Este año has sido bueno: te traigo un peón pasado."],
  },
  "theme-python": {
    calm: ["Tema Python. Tablero bien indentado.", "Python. Legible, limpio y sin llaves."],
    funny: ["Python. import alfil; alfil.move(diagonal=True)", "Python. Cuidado, que aquí la serpiente se come a los peones."],
  },
  "theme-java": {
    calm: ["Tema Java. Como el servidor de este juego.", "Java. Café recién hecho para pensar."],
    funny: ["Java. public static void moverEnDiagonal().", "Java. Soy una clase final: nadie hereda mis diagonales."],
  },
  loading: {
    calm: ["Preparando el tablero…", "Colocando las piezas…", "Sacando las piezas de la caja…", "Alineando los peones…"],
    funny: [
      "Colocando piezas. Si falta un peón, yo no he sido.",
      "Puliendo la corona del rey…",
      "Despertando a las torres. Duermen mucho.",
      "Buscando el caballo que siempre se cae debajo del sofá…",
    ],
  },
  error: {
    calm: [
      "Vaya, el servidor no ha podido con eso. Prueba otro modo.",
      "Algo ha fallado al hablar con el servidor. Inténtalo de nuevo.",
    ],
    funny: [
      "El servidor ha abandonado la partida. Sin avisar, como un mal rival.",
      "El servidor se ha quedado en zugzwang. No sabe qué hacer.",
      "Error de conexión. Ni la Defensa Berlinesa es tan sólida.",
    ],
  },

  // Openings.
  openingCenter: {
    calm: [
      "{from} a {to}. Clásico, directo al centro.",
      "Abrimos por el centro. Buen comienzo.",
      "Peón a {to}. Espacio en el centro y diagonales abiertas.",
      "Primera jugada central. Los manuales lo aprueban.",
    ],
    funny: [
      "Peón al centro. Original no es, pero funciona desde hace siglos.",
      "«Mejor por test», que diría Fischer de 1.e4.",
      "Peón a {to}. Las bases de datos tienen millones de partidas así. Una más.",
      "Al centro y sin miedo. Así empiezan las leyendas. Y los mates del pastor.",
    ],
  },
  openingWeird: {
    calm: [
      "Una apertura poco habitual. Veamos adónde lleva.",
      "Salida de flanco. Menos teoría y más ideas propias.",
    ],
    funny: [
      "¿{to}? Interesante. Y por interesante quiero decir rara.",
      "Apertura experimental. Los libros de teoría lloran en silencio.",
      "¿Eso es la Grob? ¿La Sokolsky? ¿O un despiste?",
      "Hipermoderno. O eso diremos si sale bien.",
    ],
  },
  openingOther: {
    calm: [
      "Primera jugada hecha. Que empiece el baile.",
      "Una salida sólida. Ya veremos qué estructura sale.",
      "Primera jugada. Réti estaría atento.",
    ],
    funny: [
      "Primera jugada. Todavía no te has dejado nada. Todavía.",
      "Jugada uno. Quedan solo unas cuarenta más, si todo va bien.",
    ],
  },
  earlyQueen: {
    calm: [
      "Sacar la dama tan pronto puede costar tiempos.",
      "La dama fuera pronto se convierte en un blanco fácil.",
      "Ojo: el rival puede desarrollarse atacando a tu dama.",
    ],
    funny: [
      "¿Dama fuera en la jugada {ply}? Huele a mate del pastor.",
      "Dama a {to} tan pronto. Beth Harmon no lo aprobaría.",
      "La dama ha salido de paseo antes que los caballos. Muy moderna.",
      "Dama a {to}. Los alfiles seguimos en casa, esperando invitación.",
    ],
  },

  // Piece moves.
  castle: {
    calm: [
      "Enroque. El rey se pone a cubierto.",
      "Buen enroque: rey seguro, torre activa.",
      "Enroque hecho. Ahora las torres pueden conectarse.",
      "El rey se refugia. Una tarea menos.",
    ],
    funny: [
      "El rey se esconde detrás de la torre. Muy valiente, majestad.",
      "Enroque: la única jugada en la que mueves dos piezas y nadie se queja.",
      "El rey ya está en su castillo. Literalmente.",
    ],
  },
  kingWalk: {
    calm: [
      "El rey sale a pasear. Cuidado con los jaques.",
      "Rey a {to}. Con muchas piezas en el tablero, eso es arriesgado.",
      "El rey se mueve sin enrocar. Vigila las columnas abiertas.",
    ],
    funny: [
      "Rey a {to}. Turismo real en plena batalla.",
      "Ese rey camina como si fuera un final. Spoiler: no lo es.",
      "El rey ha salido a por tabaco. Esperemos que vuelva.",
      "Rey a {to}. Steinitz decía que el rey se defiende solo. No te fíes.",
    ],
  },
  bongcloud: {
    calm: [
      "¿El rey sale tan pronto? Eso es el Bongcloud.",
      "Rey a {to} en la apertura. Pierde el enroque y no ayuda al desarrollo.",
    ],
    funny: [
      "¡Bongcloud! Hikaru estaría orgulloso. Tus profesores, no tanto.",
      "El Bongcloud. Hasta Carlsen lo ha jugado. En broma, eso sí.",
      "Rey a {to}. La teoría no cubre esto porque nadie se atreve.",
    ],
  },
  knightRim: {
    calm: [
      "Caballo a {to}: en el borde controla pocas casillas.",
      "Desde {to} el caballo tiene la mitad de casillas que en el centro.",
    ],
    funny: [
      "Caballo en el borde. Ya sabes lo que decía Tarrasch.",
      "Caballo a {to}. Está mirando la pared, pobrecito.",
      "Caballo a {to}. Quizá busque una salida de emergencia.",
    ],
  },
  fianchetto: {
    calm: [
      "Fianchetto: el alfil en {to} domina la gran diagonal.",
      "Alfil a {to}. Esa diagonal larga es suya.",
      "Fianchetto. Un clásico de la India de Rey y la Dragón.",
    ],
    funny: [
      "¡Fianchetto! Un alfil en su diagonal favorita. Me emociono.",
      "Alfil a {to}. Esa diagonal tiene mejores vistas que un ático.",
    ],
  },
  pawnRush: {
    calm: [
      "Ese peón de {to} empieza a ser peligroso.",
      "Peón avanzado. Ojo con la coronación.",
      "Peón a {to}. Cuanto más avanza, más vale.",
      "Un peón pasado tan avanzado obliga al rival a bloquearlo.",
    ],
    funny: [
      "El peón de {to} huele la octava fila. Y huele bien.",
      "Peón a {to}. Ese chaval tiene aspiraciones de dama.",
      "El peón de {to} ya está ensayando el discurso de coronación.",
    ],
  },
  repeat: {
    calm: [
      "Las piezas van y vienen. Tres repeticiones y son tablas.",
      "Posición repetida. ¿Alguien quiere tablas?",
    ],
    funny: [
      "Adelante, atrás, adelante… ¿Esto es ajedrez o una coreografía de Battle Chess?",
      "Esta posición ya la he visto. Tengo un déjà vu en diagonal.",
      "Si repetimos un poco más, el árbitro nos manda a casa.",
    ],
  },

  // Captures.
  capture: {
    calm: [
      "Buena captura.",
      "Material ganado.",
      "Golpe de material importante.",
      "Captura en {to}. El marcador de material se mueve.",
      "{victima} fuera. Eso cambia el equilibrio.",
    ],
    funny: [
      "¡Ñam! Esa pieza no volverá a ver la luz del día.",
      "En Battle Chess esto vendría con una animación de espadazo.",
      "{victima} capturada en {to}. Que alguien llame a una ambulancia.",
      "Captura en {to}. Hoy se come bien en este tablero.",
    ],
  },
  minorWon: {
    calm: [
      "Pieza ganada: {victima} fuera del tablero.",
      "Buena captura. Esa pieza ya no molesta.",
      "Pieza de más. Ahora toca convertir la ventaja.",
    ],
    funny: [
      "¡Ñam! {victima} al bolsillo.",
      "Una pieza menor menos. Mayor alegría.",
      "{victima} cazado en {to}. Ni lo vio venir.",
    ],
  },
  bishopLost: {
    calm: [
      "Ha caído un alfil. Un minuto de silencio, por favor.",
      "Alfil capturado en {to}. Adiós a esa diagonal.",
    ],
    funny: [
      "¡Noo, un alfil! Era como un hermano para mí.",
      "Han capturado a un colega. Esto se ha vuelto personal.",
      "Un alfil menos en el mundo. Lo apunto en mi lista negra.",
      "Alfil caído en {to}. Me tomo el resto de la partida libre.",
    ],
  },
  trade: {
    calm: [
      "Cambio de piezas. Todo sigue igualado.",
      "Intercambio limpio.",
      "Cambio en {to}. Menos piezas, más cerca del final.",
      "Pieza por pieza. El material sigue equilibrado.",
    ],
    funny: [
      "Tú me das una, yo te doy otra. Así se simplifica hacia el final.",
      "Cambio de cromos en {to}. Nadie sale perdiendo. De momento.",
    ],
  },
  enPassant: {
    calm: [
      "¡Captura al paso! Una regla que mucha gente no conoce.",
      "Al paso: solo se puede justo después del avance doble del peón rival.",
    ],
    funny: [
      "¡En passant! Holy hell.",
      "¡Al paso! Ese peón no se lo esperaba. Nadie se lo espera nunca.",
      "Captura al paso. Google, busca «en passant».",
    ],
  },
  queenLost: {
    calm: [
      "Se ha perdido la dama. Eso duele.",
      "Sin dama la cosa se complica mucho.",
      "Dama capturada en {to}. Toca reorganizarse.",
    ],
    funny: [
      "Adiós, dama. Ni el ajedrez mágico de Harry Potter era tan brutal.",
      "La dama ha caído en {to}. El rey va a necesitar terapia.",
      "Sin dama. Ahora sí que se juega en modo difícil.",
    ],
  },
  queenWon: {
    calm: [
      "¡Dama capturada! Golpe enorme.",
      "Has cazado la dama rival. Partida muy encarrilada.",
      "Dama ganada. Ahora simplifica y no te relajes.",
    ],
    funny: [
      "¡La dama ha caído! Que alguien avise a la familia real.",
      "Dama capturada en {to}. El rey rival se ha quedado viudo.",
      "¡Ñam, una dama! Eso son nueve puntos de golpe.",
    ],
  },
  blunder: {
    calm: [],
    funny: [
      "Buen trabajo.",
      "Eres bueno, ¿eh?",
      "Brillante. De verdad. Doble interrogación.",
      "Seguro que era parte del plan.",
      "Sacrificio genial. Sin compensación, pero genial.",
      "Apuntado en mi libreta de jugadas memorables.",
      "Ni Tal sacrificaba piezas así. Él al menos tenía un motivo.",
      "Stockfish acaba de tener un escalofrío.",
      "Regalar material así. Qué generosidad.",
      "Esto lo enseñan en los cursos. En el capítulo de lo que no hay que hacer.",
      "Mmm. No. Bueno, sí. No.",
      "Esa pieza tenía familia, ¿sabes?",
      "Interesante decisión. Muy… interesante.",
      "Tranquilo, ni Chigorin acertaba siempre.",
    ],
  },

  // Game state.
  check: {
    calm: [
      "¡Jaque!",
      "Jaque al rey.",
      "¡Jaque! El rey tiene que moverse o protegerse.",
      "Jaque. Tapar, capturar o huir: esas son las opciones.",
      "Jaque desde {to}.",
    ],
    funny: [
      "¡Jaque! Esto no se arregla deshaciendo la jugada.",
      "Toc, toc. ¿Quién es? Un jaque.",
      "¡Jaque! El rey acaba de derramar el café.",
      "Jaque. Majestad, alguien le busca en la puerta.",
    ],
  },
  checkAgain: {
    calm: ["Otro jaque. La presión no afloja.", "Jaque tras jaque.", "Jaque de nuevo. ¿Habrá jaque perpetuo?"],
    funny: [
      "Jaque otra vez. Ese rey ya no sabe dónde meterse.",
      "Jaque. Y otro. Y otro. El rey ya pide un descanso.",
    ],
  },
  promotion: {
    calm: [
      "¡Coronación!",
      "El peón ha llegado al final.",
      "Peón coronado en {to}. Pieza nueva en el tablero.",
    ],
    funny: [
      "De peón a dama. El ascenso más rápido del reino.",
      "¡Coronación! De soldado raso a reina en una jugada.",
      "El peón ha llegado a {to}. Nadie creía en él. Bueno, yo un poco.",
    ],
  },
  bigLead: {
    calm: [
      "Ventaja clara de las {bando}.",
      "Las {bando} van muy por delante en material.",
      "Las {bando} mandan. Ahora toca rematar sin prisas.",
    ],
    funny: [
      "Las {bando} van muy por delante. Otros abandonarían ya.",
      "Las {bando} juegan con una torre de regalo. Qué suerte.",
    ],
  },
  comeback: {
    calm: [
      "¡Remontada! El material ha cambiado de manos.",
      "La partida ha dado la vuelta.",
      "Ahora son las {bando} las que van por delante.",
    ],
    funny: [
      "¡Giro de guion! Ni la Inmortal de Anderssen tuvo tantos cambios.",
      "Remontada. Esto parece un final de película.",
      "Las {bando} han dado la vuelta a la tortilla. Española, claro.",
    ],
  },
  milestone: {
    calm: [
      "Ya van {moves} jugadas. Partida trabajada.",
      "Jugada {moves}. Esto se pone serio.",
      "{moves} jugadas. Buen momento para revisar el plan.",
    ],
    funny: [
      "{moves} jugadas. Ya hemos salido de la teoría, oficialmente.",
      "Jugada {moves}. Si fuera torneo, ya estarían pidiendo café.",
    ],
  },
  longGame: {
    calm: [
      "Partida larga. Paciencia y concentración.",
      "Muchas jugadas ya. En los finales, cada tiempo cuenta.",
    ],
    funny: [
      "Partida larguísima. Ojo, que existe la regla de las 50 jugadas.",
      "Esto ya dura más que algunas partidas de Karpov y Kasparov.",
    ],
  },
  quickMate: {
    calm: ["¡Mate en muy pocas jugadas!", "Mate rapidísimo. Repasa la apertura con calma."],
    funny: [
      "¿Mate tan rápido? Eso tiene nombre: mate del loco. Literalmente.",
      "Mate en un suspiro. Ni ha dado tiempo a enrocar.",
    ],
  },
  victory: {
    calm: [
      "¡Victoria! Bien jugado.",
      "¡Partida ganada!",
      "Jaque mate. Partida redonda.",
      "¡Ganaste! Repasa la partida para ver qué funcionó.",
    ],
    funny: [
      "Parece que has ganado. ¿Quieres celebrarlo? ¡SÍ!",
      "Mate. Esta va directa a mi colección de partidas inmortales.",
      "Victoria. Morphy te habría invitado a la ópera.",
      "¡Ganaste! Voy a enmarcar esta partida.",
    ],
  },
  defeat: {
    calm: [
      "Derrota. La próxima será.",
      "Partida perdida. A por la revancha.",
      "Mate. Repasa la partida, seguro que encuentras el fallo.",
      "Perder también enseña. Mira dónde cambió la partida.",
    ],
    funny: [
      "Hasta Kasparov perdió contra Deep Blue. Levanta esa cabeza.",
      "Derrota. Capablanca decía que se aprende más de las perdidas. Mucho aprendizaje hoy.",
      "Mate. Yo no he visto nada, tranquilo.",
    ],
  },
  draw: {
    calm: ["Tablas.", "Empate. Muy igualado.", "Tablas. Nadie pudo romper el equilibrio."],
    funny: [
      "Tablas. Ni Karpov ni Kasparov se rendían tampoco.",
      "Tablas. Todos ganan. O todos pierden. Depende de cómo lo mires.",
      "Empate. Las piezas se dan la mano y se van a casa.",
    ],
  },
};
