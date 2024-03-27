import cardStyles from './Card.module.css';
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {IconProp} from "@fortawesome/fontawesome-svg-core";
import React, {DOMElement} from "react";
import Link from "@docusaurus/Link";

type Props = {
    title: string;
    description: string;
    backgroundColor: string;
    emoji: IconProp;
    emojiColor: string;

    link?: string;
};

const parseRGB = (color: string): number[] => {
    return color.match(/#(..)(..)(..)/)!.slice(1).map((x) => parseInt(x, 16));
}

const setBrightness = (color: string, brightness: number): string => {
    const rgb = parseRGB(color);
    const newRgb = rgb.map((x) => Math.round(x * brightness));
    const hex = newRgb.map((x) => x.toString(16).padStart(2, "0")).join("");
    return "#" + hex;
}

const isLighter = (color: string): boolean => {
    const rgb = parseRGB(color);
    const brightness = rgb.reduce((a, b) => a + b) / 3;
    return brightness > 64;
}

const Card = (props: Props): JSX.Element => {
    const transparency = 0.4;

    const rawColor = props.backgroundColor;

    const rgb = parseRGB(rawColor);
    const rgba = `rgba(${rgb[0]}, ${rgb[1]}, ${rgb[2]}, ${transparency})`;
    const borderBrightness = isLighter(rawColor) ? 0.8 : 2.0;
    const borderColor = setBrightness(rawColor, borderBrightness);

    const cursor = props.link ? "pointer" : "default";

    return (
        <div className={cardStyles.card}
             style={{backgroundColor: rgba, borderColor: borderColor, cursor: cursor}}
             onClick={() => {
                 if (props.link) {
                     const link = document.querySelector(props.link) as DOMElement;
                     link.scrollIntoView({behavior: "smooth"});
                 }
             }}
        >
            <div className={cardStyles.emoji} style={{color: props.emojiColor}}>
                <FontAwesomeIcon icon={props.emoji} />
            </div>
            <div className={cardStyles.text}>
                <h3>{props.title}</h3>
                <p>{props.description}</p>
            </div>
            {props.link ? <Link to={props.link} style={{display: "none"}} /> : null}
        </div>
    );
}

export default Card;
