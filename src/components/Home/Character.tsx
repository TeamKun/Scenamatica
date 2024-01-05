import React from "react";

const Character =(props: {rotate: number, name: string, style?: React.CSSProperties}): JSX.Element => {
    const image = "/img/" + props.name + ".png"
    const rotate = props.rotate

    return (
            <img
                style={{
                    position: "absolute",
                    height: "100px",
                    width: "100px",
                    userSelect: "none",
                    transform: `rotate(${rotate}deg)`,
                    ...props.style
            }}
                src={image}
                alt="Character"
            />

    )
}

export default Character
