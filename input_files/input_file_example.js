{
    "solver": "2d",
    "calculationConstants": {
        "CFL": 0.5,
        "xRes": 565,
        "yRes": 565
    },
    "physicalConstants": {
        "gamma": 1.66666666666,
        "xLength": 2.0,
        "yLength": 1.0,
        "totalTime": 0.5
    },
    "restorator": {
        "type": "simple_minmod"
    },
    "conditions_input_type" : "separate",
    "border_conditions": {
        "type": "continuation"
    },
    "initial_conditions_2d": [
      {
        "type": "fill_rect",
        "x1": 0,
        "y1": 0,
        "x2": 1.4,
        "y2": 1,
        "value": {
            "rho": 3.88968,
            "u": 0.0,
            "v": 0.0,
            "w": -0.0523,
            "p": 14.2614,
            "bX": 1.0,
            "bY": 0.0,
            "bZ": 3.9353
        }
      }, 
      {
        "type": "fill_rect",
        "x1": 1.4,
        "y1": 0,
        "x2": 2,
        "y2": 1,
        "value": {
            "rho": 1.0,
            "u": -3.3156,
            "v": 0.0,
            "w": 0.0,
            "p": 0.04,
            "bX": 1.0,
            "bY": 0.0,
            "bZ": 1.0
        }
      }, 
      {
        "type": "fill_circle",
        "x": 1.4,
        "y": 0.5,
        "radius": 0.18,
        "value": {
            "rho": 5.0,
            "u": -3.3156,
            "v": 0.0,
            "w": 0.0,
            "p": 0.04,
            "bX": 1.0,
            "bY": 0.0,
            "bZ": 1.0
        }
      } 
    ]
}