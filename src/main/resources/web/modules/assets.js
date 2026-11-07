export const logoUrl = "./assets/archchess_logo.png";

export function uiIcon(name, className = "ui-icon") {
	const image = document.createElement("img");
	image.src = `./assets/ui/${name}.png`;
	image.className = className;
	image.alt = "";
	image.draggable = false;
	image.setAttribute("aria-hidden", "true");
	return image;
}

import { themes } from "./themes.js";
import { pieceBounds, classicPieceHeight } from "./piece-layouts.js";

const pieceSheetSize = { width: 2172, height: 724 };
const tileSheetSize = { width: 1774, height: 887 };
const tileFace = { size: 540, y: 175, lightX: 175, darkX: 1025 };
const redesignedTileSheetSize = { width: 512, height: 256 };
const redesignedTileFace = { size: 256, y: 0, lightX: 0, darkX: 256 };

import { colorSwapped, shownColor } from "./colors.js";

const pieceSkins = new Map();

export function setPieceSkin(color, type, create) {
	if(create) pieceSkins.set(`${color}:${type}`, create);
	else pieceSkins.delete(`${color}:${type}`);
}

export function variantTag(type, color) {
	return `${pieceSkins.has(`${color}:${type}`) ? ":skin" : ""}${colorSwapped() ? ":swap" : ""}`;
}

export function gamePiece(type, color, style = "classic") {
	const skin = pieceSkins.get(`${color}:${type}`);
	if(skin) return skin();
	return pieceImage(type, shownColor(color), style);
}

export function pieceImage(type, color, style = "classic") {
	const theme = themes[style] ?? themes.classic;
	const sprite = document.createElement("span");
	sprite.className = "piece-sprite";
	sprite.setAttribute("aria-hidden", "true");
	sprite.dataset.piece = type;
	sprite.dataset.color = color;

	const image = document.createElement("img");
	image.src = `./assets/${theme.pieces}`;
	image.alt = "";
	image.draggable = false;

	const [x, y, width, height] = pieceBounds(style, type, color);
	const displayHeight = classicPieceHeight(type);
	const artwork = document.createElement("span");
	artwork.className = "piece-art";
	artwork.style.width = `${(width / height) * displayHeight * 100}%`;
	artwork.style.height = `${displayHeight * 100}%`;
	image.style.width = `${(pieceSheetSize.width / width) * 100}%`;
	image.style.height = `${(pieceSheetSize.height / height) * 100}%`;
	image.style.left = `${(-x / width) * 100}%`;
	image.style.top = `${(-y / height) * 100}%`;
	artwork.append(image);
	sprite.append(artwork);
	return sprite;
}

export function setBoardTiles(board, style) {
	const theme = themes[style] ?? themes.classic;
	const redesigned = theme.spriteLayout === "redesigned";
	const sheetSize = redesigned ? redesignedTileSheetSize : tileSheetSize;
	const face = redesigned ? redesignedTileFace : tileFace;
	board.style.setProperty("--tile-sheet", `url("./assets/${theme.tiles}")`);
	board.style.setProperty(
		"--tile-size",
		`${(sheetSize.width / face.size) * 100}% ${(sheetSize.height / face.size) * 100}%`,
	);
	board.style.setProperty("--tile-light-x", tileOffset(face.lightX, sheetSize.width, face.size));
	board.style.setProperty("--tile-dark-x", tileOffset(face.darkX, sheetSize.width, face.size));
	board.style.setProperty("--tile-y", tileOffset(face.y, sheetSize.height, face.size));
}

function tileOffset(position, sheetSize, faceSize) {
	if(sheetSize === faceSize) return "0%";
	return `${(position / (sheetSize - faceSize)) * 100}%`;
}
