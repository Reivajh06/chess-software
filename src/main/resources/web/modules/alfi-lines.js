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
    ],
    funny: [
      "Parece que intentas jugar al ajedrez. ¿Quieres ayuda?",
      "Mi abuelo era un clip de oficina. Yo prefiero las diagonales.",
      "Kasparov tenía a Deep Blue. Tú me tienes a mí. Sales ganando.",
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
    ],
    funny: [
      "Tarrasch decía que un caballo en el borde es un caballo triste. Tenía razón.",
      "Dos alfiles juntos son una pareja peligrosa. Lo digo por experiencia.",
      "Estoy leyendo «Mi sistema» de Nimzowitsch. Va por el capítulo del bloqueo.",
      "Dato: el alfil se llamaba «al-fil», elefante en árabe. Respeta mi linaje.",
      "Capablanca casi nunca estudiaba aperturas. No lo intentes en casa.",
      "Morphy ganó la Partida de la Ópera mientras sonaba una ópera. Aquí suena chiptune.",
    ],
  },
  start: {
    calm: ["¡Que empiece la partida!", "Tablero listo. ¡Suerte!", "Piezas colocadas. Las blancas abren el baile."],
    funny: [
      "Nueva partida, nuevas oportunidades de dejarse una pieza. ¡Ánimo!",
      "Las piezas están en su sitio. Por ahora.",
    ],
  },
  loading: {
    calm: ["Preparando el tablero…", "Colocando las piezas…"],
    funny: ["Colocando piezas. Si falta un peón, yo no he sido."],
  },
  error: {
    calm: ["Vaya, el servidor no ha podido con eso. Prueba otro modo."],
    funny: ["El servidor ha abandonado la partida. Sin avisar, como un mal rival."],
  },

  // Openings.
  openingCenter: {
    calm: ["{from} a {to}. Clásico, directo al centro.", "Abrimos por el centro. Buen comienzo."],
    funny: [
      "Peón al centro. Original no es, pero funciona desde hace siglos.",
      "«Mejor por test», que diría Fischer de 1.e4.",
    ],
  },
  openingWeird: {
    calm: ["Una apertura poco habitual. Veamos adónde lleva."],
    funny: [
      "¿{to}? Interesante. Y por interesante quiero decir rara.",
      "Apertura experimental. Los libros de teoría lloran en silencio.",
    ],
  },
  openingOther: {
    calm: ["Primera jugada hecha. Que empiece el baile."],
    funny: ["Primera jugada. Todavía no te has dejado nada. Todavía."],
  },
  earlyQueen: {
    calm: ["Sacar la dama tan pronto puede costar tiempos."],
    funny: [
      "¿Dama fuera en la jugada {ply}? Huele a mate del pastor.",
      "Dama a {to} tan pronto. Beth Harmon no lo aprobaría.",
    ],
  },

  // Piece moves.
  castle: {
    calm: ["Enroque. El rey se pone a cubierto.", "Buen enroque: rey seguro, torre activa."],
    funny: ["El rey se esconde detrás de la torre. Muy valiente, majestad."],
  },
  kingWalk: {
    calm: ["El rey sale a pasear. Cuidado con los jaques."],
    funny: [
      "Rey a {to}. Turismo real en plena batalla.",
      "Ese rey camina como si fuera un final. Spoiler: no lo es.",
    ],
  },
  bongcloud: {
    calm: ["¿El rey sale tan pronto? Eso es el Bongcloud."],
    funny: ["¡Bongcloud! Hikaru estaría orgulloso. Tus profesores, no tanto."],
  },
  knightRim: {
    calm: ["Caballo a {to}: en el borde controla pocas casillas."],
    funny: ["Caballo en el borde. Ya sabes lo que decía Tarrasch.", "Caballo a {to}. Está mirando la pared, pobrecito."],
  },
  fianchetto: {
    calm: ["Fianchetto: el alfil en {to} domina la gran diagonal.", "Alfil a {to}. Esa diagonal larga es suya."],
    funny: ["¡Fianchetto! Un alfil en su diagonal favorita. Me emociono."],
  },
  pawnRush: {
    calm: ["Ese peón de {to} empieza a ser peligroso.", "Peón avanzado. Ojo con la coronación."],
    funny: ["El peón de {to} huele la octava fila. Y huele bien."],
  },
  repeat: {
    calm: ["Las piezas van y vienen. Tres repeticiones y son tablas."],
    funny: ["Adelante, atrás, adelante… ¿Esto es ajedrez o una coreografía de Battle Chess?"],
  },

  // Captures.
  capture: {
    calm: ["Buena captura.", "Material ganado.", "Golpe de material importante."],
    funny: ["¡Ñam! Esa pieza no volverá a ver la luz del día.", "En Battle Chess esto vendría con una animación de espadazo."],
  },
  minorWon: {
    calm: ["Pieza ganada: {victima} fuera del tablero.", "Buena captura. Esa pieza ya no molesta."],
    funny: ["¡Ñam! {victima} al bolsillo.", "Una pieza menor menos. Mayor alegría."],
  },
  bishopLost: {
    calm: ["Ha caído un alfil. Un minuto de silencio, por favor."],
    funny: ["¡Noo, un alfil! Era como un hermano para mí.", "Han capturado a un colega. Esto se ha vuelto personal."],
  },
  trade: {
    calm: ["Cambio de piezas. Todo sigue igualado.", "Intercambio limpio."],
    funny: ["Tú me das una, yo te doy otra. Así se simplifica hacia el final."],
  },
  enPassant: {
    calm: ["¡Captura al paso! Una regla que mucha gente no conoce."],
    funny: ["¡En passant! Holy hell.", "¡Al paso! Ese peón no se lo esperaba. Nadie se lo espera nunca."],
  },
  queenLost: {
    calm: ["Se ha perdido la dama. Eso duele.", "Sin dama la cosa se complica mucho."],
    funny: ["Adiós, dama. Ni el ajedrez mágico de Harry Potter era tan brutal."],
  },
  queenWon: {
    calm: ["¡Dama capturada! Golpe enorme.", "Has cazado la dama rival. Partida muy encarrilada."],
    funny: ["¡La dama ha caído! Que alguien avise a la familia real."],
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
    ],
  },

  // Game state.
  check: {
    calm: ["¡Jaque!", "Jaque al rey.", "¡Jaque! El rey tiene que moverse o protegerse."],
    funny: ["¡Jaque! Esto no se arregla deshaciendo la jugada.", "Toc, toc. ¿Quién es? Un jaque."],
  },
  checkAgain: {
    calm: ["Otro jaque. La presión no afloja.", "Jaque tras jaque."],
    funny: ["Jaque otra vez. Ese rey ya no sabe dónde meterse."],
  },
  promotion: {
    calm: ["¡Coronación!", "El peón ha llegado al final."],
    funny: ["De peón a dama. El ascenso más rápido del reino."],
  },
  bigLead: {
    calm: ["Ventaja clara de las {bando}.", "Las {bando} van muy por delante en material."],
    funny: ["Las {bando} van muy por delante. Otros abandonarían ya."],
  },
  comeback: {
    calm: ["¡Remontada! El material ha cambiado de manos.", "La partida ha dado la vuelta."],
    funny: ["¡Giro de guion! Ni la Inmortal de Anderssen tuvo tantos cambios."],
  },
  milestone: {
    calm: ["Ya van {moves} jugadas. Partida trabajada.", "Jugada {moves}. Esto se pone serio."],
    funny: ["{moves} jugadas. Ya hemos salido de la teoría, oficialmente."],
  },
  longGame: {
    calm: ["Partida larga. Paciencia y concentración."],
    funny: ["Partida larguísima. Ojo, que existe la regla de las 50 jugadas."],
  },
  quickMate: {
    calm: ["¡Mate en muy pocas jugadas!"],
    funny: ["¿Mate tan rápido? Eso tiene nombre: mate del loco. Literalmente."],
  },
  victory: {
    calm: ["¡Victoria! Bien jugado.", "¡Partida ganada!", "Jaque mate. Partida redonda."],
    funny: ["Parece que has ganado. ¿Quieres celebrarlo? ¡SÍ!", "Mate. Esta va directa a mi colección de partidas inmortales."],
  },
  defeat: {
    calm: ["Derrota. La próxima será.", "Partida perdida. A por la revancha.", "Mate. Repasa la partida, seguro que encuentras el fallo."],
    funny: ["Hasta Kasparov perdió contra Deep Blue. Levanta esa cabeza."],
  },
  draw: {
    calm: ["Tablas.", "Empate. Muy igualado."],
    funny: ["Tablas. Ni Karpov ni Kasparov se rendían tampoco."],
  },
};
