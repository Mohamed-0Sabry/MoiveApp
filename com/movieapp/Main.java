/* General Styles */
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
    font-family: 'Arial', sans-serif;
}

body {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    background: linear-gradient(135deg, #0099cc, #66ccff); /* Background gradient */
    color: #fff;
    text-align: center;
}

.container {
    background-color: rgba(255, 255, 255, 0.8);
    border-radius: 20px;
    padding: 40px 20px;
    max-width: 600px;
    width: 100%;
}

/* MovieApp Title */
h1 {
    font-size: 3rem;
    font-weight: bold;
    color: white;
}

/* Tagline */
h2 {
    font-size: 1.2rem;
    color: #eeeeee;
    margin-bottom: 20px;
}

/* Logo Image */
img.logo {
    width: 150px;
    height: auto;
    margin: 20px 0;
}

/* Buttons */
button {
    background-color: #ffffff;
    color: #007bff;
    font-size: 1.2rem;
    padding: 12px 30px;
    border: none;
    border-radius: 30px;
    cursor: pointer;
    transition: background-color 0.3s ease, transform 0.2s ease;
    margin: 10px;
}

button:hover {
    background-color: #007bff;
    color: white;
    transform: translateY(-4px);
}

/* Footer */
footer {
    margin-top: 20px;
    font-size: 0.9rem;
    color: #aaa;
}
