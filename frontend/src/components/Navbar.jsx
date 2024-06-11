import React from 'react'
import { NavLink } from 'react-router-dom'

const Navbar = () => {
    return (
        <>
            <nav className='flex justify-between p-4 bg-gray-800 text-white'>
                <div className='font-bold text-lg'>
                    <span className='text-teal-400'>Cpp</span>Run
                </div>
                <div className='space-x-4 flex items-center'>
                    <NavLink
                        to="/"
                        className={(obj) => obj.isActive ? "text-teal-400" : "text-white"}
                    >
                        Home
                    </NavLink>
                    <NavLink
                        to="/about"
                        className={(obj) => obj.isActive ? "text-teal-400" : "text-white"}
                    >
                        about
                    </NavLink>
                    <NavLink
                        to="/contact"
                        className={(obj) => obj.isActive ? "text-teal-400" : "text-white"}
                    >
                        contact us
                    </NavLink>
                </div>
            </nav>
        </>
    )
}

export default Navbar
